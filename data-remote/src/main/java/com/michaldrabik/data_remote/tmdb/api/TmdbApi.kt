package com.michaldrabik.data_remote.tmdb.api

import com.michaldrabik.data_remote.tmdb.TmdbRemoteDataSource
import com.michaldrabik.data_remote.tmdb.model.TmdbImages
import com.michaldrabik.data_remote.tmdb.model.TmdbPerson
import com.michaldrabik.data_remote.tmdb.model.TmdbStreamingCountry
import com.michaldrabik.data_remote.tmdb.model.TmdbTranslation

internal class TmdbApi(
  private val service: TmdbService,
) : TmdbRemoteDataSource {

  override suspend fun fetchShowImages(tmdbId: Long) =
    try {
      if (tmdbId <= 0) TmdbImages.EMPTY
      service.fetchShowImages(tmdbId)
    } catch (error: Throwable) {
      TmdbImages.EMPTY
    }

  override suspend fun fetchEpisodeImage(
    showTmdbId: Long?,
    season: Int?,
    episode: Int?,
  ) = try {
    if (showTmdbId == null || showTmdbId <= 0) TmdbImages.EMPTY
    if (season == null || season <= 0) TmdbImages.EMPTY
    if (episode == null || episode <= 0) TmdbImages.EMPTY
    val images = service.fetchEpisodeImages(showTmdbId, season, episode)
    images.stills?.firstOrNull()
  } catch (error: Throwable) {
    null
  }

  override suspend fun fetchMovieImages(tmdbId: Long) =
    try {
      if (tmdbId <= 0) TmdbImages.EMPTY
      service.fetchMovieImages(tmdbId)
    } catch (error: Throwable) {
      TmdbImages.EMPTY
    }

  override suspend fun fetchMoviePeople(tmdbId: Long): Map<TmdbPerson.Type, List<TmdbPerson>> {
    val result = service.fetchMoviePeople(tmdbId)
    val cast = result.cast?.toList() ?: emptyList()
    val crew = result.crew?.toList() ?: emptyList()
    return mapOf(
      TmdbPerson.Type.CAST to cast,
      TmdbPerson.Type.CREW to crew,
    )
  }

  override suspend fun fetchShowPeople(tmdbId: Long): Map<TmdbPerson.Type, List<TmdbPerson>> {
    val result = service.fetchShowPeople(tmdbId)
    val cast = result.cast?.toList() ?: emptyList()
    val crew = result.crew?.toList() ?: emptyList()
    return mapOf(
      TmdbPerson.Type.CAST to cast,
      TmdbPerson.Type.CREW to crew,
    )
  }

  override suspend fun fetchShowWatchProviders(
    tmdbId: Long,
    countryCode: String,
  ): TmdbStreamingCountry? {
    val result = service.fetchShowWatchProviders(tmdbId)
    val code = when (countryCode.uppercase()) {
      "UK" -> "GB"
      else -> countryCode.uppercase()
    }
    return result.results[code]
  }

  override suspend fun fetchMovieWatchProviders(
    tmdbId: Long,
    countryCode: String,
  ): TmdbStreamingCountry? {
    val result = service.fetchMovieWatchProviders(tmdbId)
    val code = when (countryCode.uppercase()) {
      "UK" -> "GB"
      else -> countryCode.uppercase()
    }
    return result.results[code]
  }

  override suspend fun fetchPersonDetails(id: Long): TmdbPerson = service.fetchPersonDetails(id)

  override suspend fun fetchPersonTranslations(id: Long): Map<String, TmdbTranslation.Data> {
    val result = service.fetchPersonTranslation(id).translations ?: emptyList()
    return result
      .filter {
        if (it.iso_639_1.lowercase() != "zh") true else it.iso_3166_1.lowercase() == "cn"
      } // Chinese Simplified filter
      .associateBy(
        keySelector = { it.iso_639_1.lowercase() },
        valueTransform = { it.data ?: TmdbTranslation.Data(null) },
      )
  }

  override suspend fun fetchPersonImages(tmdbId: Long) =
    try {
      if (tmdbId <= 0) TmdbImages.EMPTY
      service.fetchPersonImages(tmdbId)
    } catch (error: Throwable) {
      TmdbImages.EMPTY
    }

  override suspend fun fetchMovieRelease(imdbId: Long) =
    service.fetchMovieRelease(imdbId)
}
