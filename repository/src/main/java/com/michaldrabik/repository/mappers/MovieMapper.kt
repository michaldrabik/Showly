package com.michaldrabik.repository.mappers

import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.MovieStatus
import java.time.LocalDate
import javax.inject.Inject
import com.michaldrabik.data_local.database.model.Movie as MovieDb
import com.michaldrabik.data_remote.trakt.model.Movie as MovieNetwork

class MovieMapper @Inject constructor(
  private val idsMapper: IdsMapper,
) {

  fun fromNetwork(movie: MovieNetwork): Movie {
    val released: LocalDate? = movie.released?.let { if (it.isNotBlank()) LocalDate.parse(it) else null }

    return Movie(
      idsMapper.fromNetwork(movie.ids),
      movie.title ?: "",
      movie.year ?: -1,
      movie.overview ?: "",
      released,
      released,
      movie.runtime ?: -1,
      movie.country ?: "",
      movie.country,
      movie.trailer ?: "",
      movie.homepage ?: "",
      movie.language ?: "",
      MovieStatus.fromKey(movie.status),
      movie.rating ?: -1F,
      movie.votes ?: -1,
      movie.comment_count ?: -1,
      movie.genres ?: emptyList(),
      nowUtcMillis(),
      nowUtcMillis()
    )
  }

  fun toNetwork(movie: Movie) =
    MovieNetwork(
      idsMapper.toNetwork(movie.ids),
      movie.title,
      movie.year,
      movie.overview,
      movie.released?.toString(),
      movie.runtime,
      movie.country,
      movie.trailer,
      movie.homepage,
      movie.status.key,
      movie.rating,
      movie.votes,
      movie.commentCount,
      movie.genres,
      movie.language,
    )

  fun fromDatabase(movie: MovieDb) = Movie(
    idsMapper.fromDatabase(movie),
    movie.title,
    movie.year,
    movie.overview,
    movie.released.let { if (it.isNotBlank()) LocalDate.parse(it) else null },
    movie.originalRelease.let { if (it.isNotBlank()) LocalDate.parse(it) else null },
    movie.runtime,
    movie.country,
    movie.currentCountry.let { it.ifBlank { null } },
    movie.trailer,
    movie.homepage,
    movie.language,
    MovieStatus.fromKey(movie.status),
    movie.rating,
    movie.votes,
    movie.commentCount,
    movie.genres.split(","),
    movie.updatedAt,
    movie.createdAt
  )

  fun toDatabase(movie: Movie) = MovieDb(
    movie.ids.trakt.id,
    movie.ids.tmdb.id,
    movie.ids.imdb.id,
    movie.ids.slug.id,
    movie.title,
    movie.year,
    movie.overview,
    movie.released?.toString() ?: "",
    movie.originalRelease?.toString() ?: "",
    movie.runtime,
    movie.country,
    movie.currentCountry ?: "",
    movie.trailer,
    movie.language,
    movie.homepage,
    movie.status.key,
    movie.rating,
    movie.votes,
    movie.commentCount,
    movie.genres.joinToString(","),
    nowUtcMillis(),
    movie.createdAt
  )
}