package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class Movie(
  @PrimaryKey @ColumnInfo(name = "id_trakt") val idTrakt: Long,
  @ColumnInfo(name = "id_tmdb", defaultValue = "-1") val idTmdb: Long,
  @ColumnInfo(name = "id_imdb", defaultValue = "") val idImdb: String,
  @ColumnInfo(name = "id_slug", defaultValue = "") val idSlug: String,
  @ColumnInfo(name = "title", defaultValue = "") val title: String,
  @ColumnInfo(name = "year", defaultValue = "-1") val year: Int,
  @ColumnInfo(name = "overview", defaultValue = "") val overview: String,
  @ColumnInfo(name = "released", defaultValue = "") var released: String,
  @ColumnInfo(name = "original_release", defaultValue = "") val originalRelease: String,
  @ColumnInfo(name = "runtime", defaultValue = "-1") val runtime: Int,
  @ColumnInfo(name = "country", defaultValue = "") val country: String,
  @ColumnInfo(name = "current_country", defaultValue = "") val currentCountry: String,
  @ColumnInfo(name = "trailer", defaultValue = "") val trailer: String,
  @ColumnInfo(name = "language", defaultValue = "") val language: String,
  @ColumnInfo(name = "homepage", defaultValue = "") val homepage: String,
  @ColumnInfo(name = "status", defaultValue = "") val status: String,
  @ColumnInfo(name = "rating", defaultValue = "-1") val rating: Float,
  @ColumnInfo(name = "votes", defaultValue = "-1") val votes: Long,
  @ColumnInfo(name = "comment_count", defaultValue = "-1") val commentCount: Long,
  @ColumnInfo(name = "genres", defaultValue = "") val genres: String,
  @ColumnInfo(name = "updated_at", defaultValue = "-1") val updatedAt: Long,
  @ColumnInfo(name = "created_at", defaultValue = "-1") val createdAt: Long,
)
