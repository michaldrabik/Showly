package com.michaldrabik.ui_my_movies.watchlist.recycler

import com.michaldrabik.ui_base.common.MovieListItem
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_model.Translation
import java.time.format.DateTimeFormatter

sealed class WatchlistListItem(
  override val movie: Movie,
  override val image: Image,
  override val isLoading: Boolean = false,
) : MovieListItem {

  data class FiltersItem(
    val sortOrder: SortOrder,
    val sortType: SortType,
  ) : WatchlistListItem(
    movie = Movie.EMPTY,
    image = Image.createUnknown(POSTER),
    isLoading = false
  )

  data class MovieItem(
    override val movie: Movie,
    override val image: Image,
    override val isLoading: Boolean = false,
    val translation: Translation? = null,
    val userRating: Int? = null,
    val dateFormat: DateTimeFormatter? = null,
    val fullDateFormat: DateTimeFormatter? = null,
  ) : WatchlistListItem(
    movie = movie,
    image = image,
    isLoading = isLoading
  )
}
