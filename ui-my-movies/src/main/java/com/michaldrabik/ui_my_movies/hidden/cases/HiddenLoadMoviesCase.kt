package com.michaldrabik.ui_my_movies.hidden.cases

import com.michaldrabik.common.Config
import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_model.Genre
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_model.TraktRating
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_my_movies.common.helpers.CollectionItemSorter
import com.michaldrabik.ui_my_movies.common.recycler.CollectionListItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@ViewModelScoped
class HiddenLoadMoviesCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val ratingsCase: HiddenRatingsCase,
  private val sorter: CollectionItemSorter,
  private val moviesRepository: MoviesRepository,
  private val translationsRepository: TranslationsRepository,
  private val dateFormatProvider: DateFormatProvider,
  private val imagesProvider: MovieImagesProvider,
  private val settingsRepository: SettingsRepository,
) {

  suspend fun loadMovies(searchQuery: String): List<CollectionListItem> =
    withContext(dispatchers.IO) {
      val language = translationsRepository.getLanguage()
      val ratings = ratingsCase.loadRatings()
      val dateFormat = dateFormatProvider.loadShortDayFormat()
      val fullDateFormat = dateFormatProvider.loadFullDayFormat()
      val translations =
        if (language == Config.DEFAULT_LANGUAGE) emptyMap()
        else translationsRepository.loadAllMoviesLocal(language)

      val sortOrder = settingsRepository.sorting.hiddenMoviesSortOrder
      val sortType = settingsRepository.sorting.hiddenMoviesSortType
      val genres = settingsRepository.filters.hiddenMoviesGenres

      val filtersItem = loadFiltersItem(sortOrder, sortType, genres)
      val moviesItems = moviesRepository.hiddenMovies.loadAll()
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
        .filterByGenre(genres.map { it.slug.lowercase() })
        .sortedWith(sorter.sort(sortOrder, sortType))

      if (moviesItems.isNotEmpty() || filtersItem.hasActiveFilters()) {
        listOf(filtersItem) + moviesItems
      } else {
        moviesItems
      }
    }

  private fun loadFiltersItem(
    sortOrder: SortOrder,
    sortType: SortType,
    genres: List<Genre>,
  ): CollectionListItem.FiltersItem {
    return CollectionListItem.FiltersItem(
      sortOrder = sortOrder,
      sortType = sortType,
      genres = genres,
      isUpcoming = false
    )
  }

  private fun List<CollectionListItem.MovieItem>.filterByQuery(query: String) =
    this.filter {
      it.movie.title.contains(query, true) ||
        it.translation?.title?.contains(query, true) == true
    }

  private fun List<CollectionListItem.MovieItem>.filterByGenre(genres: List<String>) =
    filter { genres.isEmpty() || it.movie.genres.any { genre -> genre.lowercase() in genres } }

  suspend fun loadTranslation(movie: Movie, onlyLocal: Boolean): Translation? =
    withContext(dispatchers.IO) {
      val language = translationsRepository.getLanguage()
      if (language == Config.DEFAULT_LANGUAGE) {
        return@withContext Translation.EMPTY
      }
      translationsRepository.loadTranslation(movie, language, onlyLocal)
    }

  private fun CoroutineScope.toListItemAsync(
    movie: Movie,
    translation: Translation?,
    userRating: TraktRating?,
    dateFormat: DateTimeFormatter,
    fullDateFormat: DateTimeFormatter,
  ) = async {
    val image = imagesProvider.findCachedImage(movie, ImageType.POSTER)
    CollectionListItem.MovieItem(
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
