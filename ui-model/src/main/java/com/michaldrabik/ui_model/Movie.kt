package com.michaldrabik.ui_model

import com.michaldrabik.common.extensions.nowUtcDay
import java.time.LocalDate

data class Movie(
  val ids: Ids,
  val title: String,
  var year: Int,
  val overview: String,
  var released: LocalDate?,
  val originalRelease: LocalDate?,
  val runtime: Int,
  val country: String,
  var currentCountry: String?,
  val trailer: String,
  val homepage: String,
  val language: String,
  val status: MovieStatus,
  val rating: Float,
  val votes: Long,
  val commentCount: Long,
  val genres: List<String>,
  val updatedAt: Long,
  val createdAt: Long,
) {

  val traktId = ids.trakt.id

  val titleNoThe = title.removePrefix("The").trim()

  fun hasNoDate() = released == null && year <= 0

  fun hasAired(): Boolean {
    if (released == null) return false
    val now = nowUtcDay()
    return now.isEqual(released) || now.isAfter(released)
  }

  fun isToday(): Boolean {
    if (released == null) return false
    val now = nowUtcDay()
    return now.isEqual(released)
  }

  companion object {
    val EMPTY = Movie(
      Ids.EMPTY,
      "",
      -1,
      "",
      null,
      null,
      -1,
      "",
      null,
      "",
      "",
      "",
      MovieStatus.UNKNOWN,
      -1F,
      -1L,
      -1L,
      emptyList(),
      -1L,
      -1L,
    )
  }
}
