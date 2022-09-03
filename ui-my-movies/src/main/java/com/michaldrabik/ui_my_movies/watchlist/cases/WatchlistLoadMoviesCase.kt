package com.michaldrabik.ui_my_movies.watchlist.cases

import com.michaldrabik.common.Config
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_model.TraktRating
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_my_movies.watchlist.helpers.WatchlistItemSorter
import com.michaldrabik.ui_my_movies.watchlist.recycler.WatchlistListItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@ViewModelScoped
class WatchlistLoadMoviesCase @Inject constructor(
  private val ratingsCase: WatchlistRatingsCase,
  private val sorter: WatchlistItemSorter,
  private val moviesRepository: MoviesRepository,
  private val translationsRepository: TranslationsRepository,
  private val dateFormatProvider: DateFormatProvider,
  private val imagesProvider: MovieImagesProvider,
  private val settingsRepository: SettingsRepository,
) {

  val language by lazy { translationsRepository.getLanguage() }

  suspend fun loadMovies(searchQuery: String): List<WatchlistListItem> = coroutineScope {
    val ratings = ratingsCase.loadRatings()
    val dateFormat = dateFormatProvider.loadShortDayFormat()
    val fullDateFormat = dateFormatProvider.loadFullDayFormat()
    val translations =
      if (language == Config.DEFAULT_LANGUAGE) emptyMap()
      else translationsRepository.loadAllMoviesLocal(language)

    val sortOrder = settingsRepository.sorting.watchlistMoviesSortOrder
    val sortType = settingsRepository.sorting.watchlistMoviesSortType

    val moviesItems = moviesRepository.watchlistMovies.loadAll()
      .map {
        toListItemAsync(
          movie = it,
          translation = translations[it.traktId],
          userRating = ratings[it.ids.trakt],
          dateFormat = dateFormat,
          fullDateFormat = fullDateFormat
        )
      }
      .awaitAll()
      .filterByQuery(searchQuery)
      .sortedWith(sorter.sort(sortOrder, sortType))

    if (moviesItems.isNotEmpty()) {
      val filtersItem = loadFiltersItem(sortOrder, sortType)
      listOf(filtersItem) + moviesItems
    } else {
      moviesItems
    }
  }

  private fun loadFiltersItem(
    sortOrder: SortOrder,
    sortType: SortType,
  ): WatchlistListItem.FiltersItem {
    return WatchlistListItem.FiltersItem(
      sortOrder = sortOrder,
      sortType = sortType
    )
  }

  private fun List<WatchlistListItem.MovieItem>.filterByQuery(query: String) =
    this.filter {
      it.movie.title.contains(query, true) ||
        it.translation?.title?.contains(query, true) == true
    }

  suspend fun loadTranslation(movie: Movie, onlyLocal: Boolean): Translation? {
    if (language == Config.DEFAULT_LANGUAGE) return Translation.EMPTY
    return translationsRepository.loadTranslation(movie, language, onlyLocal)
  }

  private fun CoroutineScope.toListItemAsync(
    movie: Movie,
    translation: Translation?,
    userRating: TraktRating?,
    dateFormat: DateTimeFormatter,
    fullDateFormat: DateTimeFormatter,
  ) = async {
    val image = imagesProvider.findCachedImage(movie, ImageType.POSTER)
    WatchlistListItem.MovieItem(
      isLoading = false,
      movie = movie,
      image = image,
      dateFormat = dateFormat,
      fullDateFormat = fullDateFormat,
      translation = translation,
      userRating = userRating?.rating
    )
  }
}
