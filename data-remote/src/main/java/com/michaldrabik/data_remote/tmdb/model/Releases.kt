package com.michaldrabik.data_remote.tmdb.model

data class Releases(
  val id: Long,
  val results: List<ReleaseInfo>
)
