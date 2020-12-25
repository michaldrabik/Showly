package com.michaldrabik.ui_discover_movies.cases

import com.michaldrabik.common.Config
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_discover_movies.recycler.DiscoverMovieListItem
import com.michaldrabik.ui_model.DiscoverFilters
import com.michaldrabik.ui_model.DiscoverSortOrder
import com.michaldrabik.ui_model.DiscoverSortOrder.HOT
import com.michaldrabik.ui_model.DiscoverSortOrder.NEWEST
import com.michaldrabik.ui_model.DiscoverSortOrder.RATING
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.ImageType.FANART_WIDE
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_repository.TranslationsRepository
import com.michaldrabik.ui_repository.UserTvdbManager
import com.michaldrabik.ui_repository.movies.MoviesRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@AppScope
class DiscoverMoviesCase @Inject constructor(
  private val moviesRepository: MoviesRepository,
  private val tvdbUserManager: UserTvdbManager,
  private val imagesProvider: MovieImagesProvider,
  private val translationsRepository: TranslationsRepository
) {

  suspend fun isCacheValid() = moviesRepository.discoverMovies.isCacheValid()

  suspend fun loadCachedMovies(filters: DiscoverFilters) = coroutineScope {
    val myIds = async { moviesRepository.myMovies.loadAllIds() }
    val watchlistIds = async { moviesRepository.watchlistMovies.loadAllIds() }
    val cachedMovies = async { moviesRepository.discoverMovies.loadAllCached() }
    val language = translationsRepository.getLanguage()

    prepareItems(
      cachedMovies.await(),
      myIds.await(),
      watchlistIds.await(),
      filters,
      language
    )
  }

  suspend fun loadRemoteMovies(filters: DiscoverFilters): List<DiscoverMovieListItem> {
    val showAnticipated = !filters.hideAnticipated
    val showCollection = !filters.hideCollection
    val genres = filters.genres.toList()

    try {
      tvdbUserManager.checkAuthorization()
    } catch (t: Throwable) {
      // Ignore at this moment
    }

    val myIds = moviesRepository.myMovies.loadAllIds()
    val watchlistIds = moviesRepository.watchlistMovies.loadAllIds()
    val collectionSize = myIds.size + watchlistIds.size

    val remoteMovies = moviesRepository.discoverMovies.loadAllRemote(showAnticipated, showCollection, collectionSize, genres)
    val language = translationsRepository.getLanguage()

    moviesRepository.discoverMovies.cacheDiscoverMovies(remoteMovies)
    return prepareItems(remoteMovies, myIds, watchlistIds, filters, language)
  }

  private suspend fun prepareItems(
    movies: List<Movie>,
    myMoviesIds: List<Long>,
    watchlistMoviesIds: List<Long>,
    filters: DiscoverFilters?,
    language: String
  ) = coroutineScope {
    val collectionIds = myMoviesIds + watchlistMoviesIds
    movies
      .filter {
        if (filters?.hideCollection == false) true
        else !collectionIds.contains(it.traktId)
      }
      .sortedBy(filters?.feedOrder ?: HOT)
      .mapIndexed { index, movie ->
        async {
          val itemType = when (index) {
            in (0..500 step 14) -> FANART_WIDE
            in (5..500 step 14), in (9..500 step 14) -> FANART
            else -> POSTER
          }
          val image = imagesProvider.findCachedImage(movie, itemType)
          val translation = loadTranslation(language, itemType, movie)
          DiscoverMovieListItem(
            movie,
            image,
            isCollected = movie.ids.trakt.id in myMoviesIds,
            isWatchlist = movie.ids.trakt.id in watchlistMoviesIds,
            translation = translation
          )
        }
      }.awaitAll()
  }

  private suspend fun loadTranslation(language: String, itemType: ImageType, movie: Movie) =
    if (language == Config.DEFAULT_LANGUAGE || itemType == POSTER) null
    else translationsRepository.loadTranslation(movie, language, true)

  private fun List<Movie>.sortedBy(order: DiscoverSortOrder) = when (order) {
    HOT -> this
    RATING -> this.sortedWith(compareByDescending<Movie> { it.votes }.thenBy { it.rating })
    NEWEST -> this.sortedWith(compareByDescending<Movie> { it.year }.thenByDescending { it.released })
  }
}
