package com.michaldrabik.repository.movies

import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.MoviesSyncLog
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.data_remote.tmdb.model.Releases
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_model.IdImdb
import com.michaldrabik.ui_model.IdSlug
import com.michaldrabik.ui_model.IdTmdb
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Movie
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class MovieDetailsRepository @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
  private val settingsRepository: SettingsRepository
) {

  suspend fun load(
    idTrakt: IdTrakt,
    force: Boolean = false,
  ): Movie {
    val local = localSource.movies.getById(idTrakt.id)
    if (force
      || local == null
      || (local.currentCountry != settingsRepository.country)
      || nowUtcMillis() - local.updatedAt > Config.MOVIE_DETAILS_CACHE_DURATION) {
      val remote = remoteSource.trakt.fetchMovie(idTrakt.id)
      val movie = mappers.movie.fromNetwork(remote)
      val releases: Releases = remoteSource.tmdb.fetchMovieRelease(movie.ids.tmdb.id)
      val localRelease = getLocalReleaseDate(releases)
      if (localRelease != null) {
        movie.released = localRelease
        movie.year = localRelease.year
        movie.currentCountry = settingsRepository.country
      } else { movie.currentCountry = null }
      localSource.movies.upsert(listOf(mappers.movie.toDatabase(movie)))
      localSource.moviesSyncLog.upsert(MoviesSyncLog(movie.traktId, nowUtcMillis()))
      return movie
    }
    return mappers.movie.fromDatabase(local)
  }

  private fun getLocalReleaseDate(releaseInfos: Releases): LocalDate? {
    val countryCode = settingsRepository.country.uppercase()
    val localReleases = releaseInfos.results.find { it.iso_3166_1 == countryCode }
    val localRelease = localReleases?.release_dates?.find { it.type > 2 }
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    return localRelease?.release_date?.let { LocalDate.parse(it.toString(), formatter) }
  }

  suspend fun find(idImdb: IdImdb): Movie? {
    val localMovie = localSource.movies.getById(idImdb.id)
    if (localMovie != null) {
      return mappers.movie.fromDatabase(localMovie)
    }
    return null
  }

  suspend fun find(idTmdb: IdTmdb): Movie? {
    val localMovie = localSource.movies.getByTmdbId(idTmdb.id)
    if (localMovie != null) {
      return mappers.movie.fromDatabase(localMovie)
    }
    return null
  }

  suspend fun find(idSlug: IdSlug): Movie? {
    val localMovie = localSource.movies.getBySlug(idSlug.id)
    if (localMovie != null) {
      return mappers.movie.fromDatabase(localMovie)
    }
    return null
  }

  suspend fun delete(idTrakt: IdTrakt) = localSource.movies.deleteById(idTrakt.id)
}
