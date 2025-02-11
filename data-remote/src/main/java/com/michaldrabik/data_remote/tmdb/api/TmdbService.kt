package com.michaldrabik.data_remote.tmdb.api

import com.michaldrabik.data_remote.tmdb.model.Releases
import com.michaldrabik.data_remote.tmdb.model.TmdbImages
import com.michaldrabik.data_remote.tmdb.model.TmdbPeople
import com.michaldrabik.data_remote.tmdb.model.TmdbPerson
import com.michaldrabik.data_remote.tmdb.model.TmdbStreamings
import com.michaldrabik.data_remote.tmdb.model.TmdbTranslationResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface TmdbService {

  @GET("tv/{tmdbId}/images")
  suspend fun fetchShowImages(
    @Path("tmdbId") tmdbId: Long,
  ): TmdbImages

  @GET("tv/{tmdbId}/season/{season}/episode/{episode}/images")
  suspend fun fetchEpisodeImages(
    @Path("tmdbId") tmdbId: Long?,
    @Path("season") seasonNumber: Int?,
    @Path("episode") episodeNumber: Int?,
  ): TmdbImages

  @GET("movie/{tmdbId}/images")
  suspend fun fetchMovieImages(
    @Path("tmdbId") tmdbId: Long,
  ): TmdbImages

  @GET("person/{tmdbId}/images")
  suspend fun fetchPersonImages(
    @Path("tmdbId") tmdbId: Long,
  ): TmdbImages

  @GET("person/{tmdbId}")
  suspend fun fetchPersonDetails(
    @Path("tmdbId") tmdbId: Long,
  ): TmdbPerson

  @GET("person/{tmdbId}/translations")
  suspend fun fetchPersonTranslation(
    @Path("tmdbId") tmdbId: Long,
  ): TmdbTranslationResponse

  @GET("movie/{tmdbId}/credits")
  suspend fun fetchMoviePeople(
    @Path("tmdbId") tmdbId: Long,
  ): TmdbPeople

  @GET("tv/{tmdbId}/aggregate_credits")
  suspend fun fetchShowPeople(
    @Path("tmdbId") tmdbId: Long,
  ): TmdbPeople

  @GET("movie/{tmdbId}/watch/providers")
  suspend fun fetchMovieWatchProviders(
    @Path("tmdbId") tmdbId: Long,
  ): TmdbStreamings

  @GET("tv/{tmdbId}/watch/providers")
  suspend fun fetchShowWatchProviders(@Path("tmdbId") tmdbId: Long): TmdbStreamings

  @GET("movie/{tmdbId}/release_dates")
  suspend fun fetchMovieRelease(@Path("tmdbId") tmdbId: Long): Releases
}
