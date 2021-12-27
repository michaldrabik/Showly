package com.michaldrabik.ui_movie.cases

import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsArchiveCase @Inject constructor(
  private val moviesRepository: MoviesRepository,
  private val pinnedItemsRepository: PinnedItemsRepository
) {

  suspend fun isArchived(movie: Movie) =
    moviesRepository.hiddenMovies.exists(movie.ids.trakt)

  suspend fun addToArchive(movie: Movie) {
    moviesRepository.hiddenMovies.insert(movie.ids.trakt)
    pinnedItemsRepository.removePinnedItem(movie)
  }

  suspend fun removeFromArchive(movie: Movie) =
    moviesRepository.hiddenMovies.delete(movie.ids.trakt)
}
