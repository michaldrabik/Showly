package com.michaldrabik.data_remote.tmdb.model

data class ReleaseInfo(
  val iso_3166_1: String,
  val release_dates: List<ReleaseDate>
)
