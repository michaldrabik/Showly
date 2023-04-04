package com.michaldrabik.ui_base.trakt.quicksync.runners

import com.michaldrabik.common.extensions.dateIsoStringFromMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.TraktSyncQueue
import com.michaldrabik.data_local.database.model.TraktSyncQueue.Type.EPISODE
import com.michaldrabik.data_local.database.model.TraktSyncQueue.Type.HIDDEN_MOVIE
import com.michaldrabik.data_local.database.model.TraktSyncQueue.Type.HIDDEN_SHOW
import com.michaldrabik.data_local.database.model.TraktSyncQueue.Type.MOVIE
import com.michaldrabik.data_local.database.model.TraktSyncQueue.Type.MOVIE_WATCHLIST
import com.michaldrabik.data_local.database.model.TraktSyncQueue.Type.SHOW_WATCHLIST
import com.michaldrabik.data_local.utilities.TransactionsProvider
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.data_remote.trakt.model.SyncExportItem
import com.michaldrabik.data_remote.trakt.model.SyncExportRequest
import com.michaldrabik.data_remote.trakt.model.SyncItem
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.trakt.TraktSyncRunner
import com.michaldrabik.ui_base.trakt.quicksync.runners.cases.QuickSyncDuplicateEpisodesCase
import com.michaldrabik.ui_base.trakt.quicksync.runners.cases.QuickSyncDuplicateMoviesCase
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuickSyncRunner @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val settingsRepository: SettingsRepository,
  private val duplicateEpisodesCase: QuickSyncDuplicateEpisodesCase,
  private val duplicateMoviesCase: QuickSyncDuplicateMoviesCase,
  userTraktManager: UserTraktManager
) : TraktSyncRunner(userTraktManager) {

  companion object {
    private const val BATCH_LIMIT = 100
    private const val DELAY = 2000L
  }

  override suspend fun run(): Int {
    Timber.d("Initialized.")
    try {
      checkAuthorization()
      val moviesEnabled = settingsRepository.isMoviesEnabled

      val historyCount = exportHistoryItems(moviesEnabled)
      val watchlistCount = exportWatchlistItems(moviesEnabled)
      val hiddenCount = exportHiddenItems(moviesEnabled)

      Timber.d("Finished with success.")
      return historyCount + watchlistCount + hiddenCount
    } catch (error: Throwable) {
      throw error
    } finally {
      localSource.traktSyncQueue.deleteAll()
    }
  }

  private suspend fun exportHistoryItems(
    moviesEnabled: Boolean,
    remoteFetchedShows: List<SyncItem> = emptyList(),
    remoteFetchedMovies: List<SyncItem> = emptyList(),
    count: Int = 0,
    clearedProgressIds: MutableSet<Long> = mutableSetOf()
  ): Int {
    val types = if (moviesEnabled) listOf(MOVIE, EPISODE) else listOf(EPISODE)
    val items = localSource.traktSyncQueue.getAll(types.map { it.slug })
    if (items.isEmpty()) {
      Timber.d("Nothing to export. Cancelling..")
      return count
    }

    Timber.d("Exporting ${items.size} items...")

    val batch = items.take(BATCH_LIMIT)
    val exportEpisodes = batch.filter { it.type == EPISODE.slug }.distinctBy { it.idTrakt }
    val exportMovies = batch.filter { it.type == MOVIE.slug }.distinctBy { it.idTrakt }
    val clearProgress = items.any { it.operation == TraktSyncQueue.Operation.ADD_WITH_CLEAR.slug }

    if (clearProgress) {
      Timber.d("Clearing progress for shows...")

      val requestItems = items
        .mapNotNull { it.idList?.let { id -> SyncExportItem.create(id) } }
        .distinctBy { it.ids.trakt }
        .filterNot { clearedProgressIds.contains(it.ids.trakt) }

      if (requestItems.isNotEmpty()) {
        remoteSource.trakt.postDeleteProgress(SyncExportRequest(shows = requestItems))
        clearedProgressIds.addAll(requestItems.map { it.ids.trakt })
        delay(DELAY)
      }
    }

    transactions.withTransaction {
      val batchIds = batch.map { it.idTrakt }
      with(localSource.traktSyncQueue) {
        deleteAll(batchIds, EPISODE.slug)
        deleteAll(batchIds, MOVIE.slug)
      }
    }

    val (duplicateEpisodes, remoteShows) =
      duplicateEpisodesCase.checkDuplicateEpisodes(exportEpisodes, remoteFetchedShows)
    val (duplicateMovies, remoteMovies) =
      duplicateMoviesCase.checkDuplicateMovies(exportMovies, remoteFetchedMovies)

    val request = SyncExportRequest(
      episodes = exportEpisodes
        .map { SyncExportItem.create(it.idTrakt, dateIsoStringFromMillis(it.updatedAt)) }
        .filter { it.ids.trakt !in duplicateEpisodes },
      movies = exportMovies
        .map { SyncExportItem.create(it.idTrakt, dateIsoStringFromMillis(it.updatedAt)) }
        .filter { it.ids.trakt !in duplicateMovies },
    )

    if (request.episodes.isNotEmpty() || request.movies.isNotEmpty()) {
      remoteSource.trakt.postSyncWatched(request)
    }

    val currentCount = count + exportEpisodes.count() + exportMovies.count()

    // Check for more items
    val newItems = localSource.traktSyncQueue.getAll(types.map { it.slug })
    if (newItems.isNotEmpty()) {
      delay(DELAY)
      return exportHistoryItems(
        moviesEnabled = moviesEnabled,
        remoteFetchedShows = remoteShows,
        remoteFetchedMovies = remoteMovies,
        count = currentCount,
        clearedProgressIds = clearedProgressIds.toMutableSet()
      )
    }

    return currentCount
  }

  private suspend fun exportWatchlistItems(
    moviesEnabled: Boolean,
    count: Int = 0
  ): Int {
    val types = if (moviesEnabled) listOf(MOVIE_WATCHLIST, SHOW_WATCHLIST) else listOf(SHOW_WATCHLIST)
    val items = localSource.traktSyncQueue.getAll(types.map { it.slug }).take(BATCH_LIMIT)
    if (items.isEmpty()) {
      Timber.d("Nothing to export. Cancelling..")
      return count
    }

    Timber.d("Exporting watchlist items...")

    val exportShows = items.filter { it.type == SHOW_WATCHLIST.slug }.distinctBy { it.idTrakt }
    val exportMovies = items.filter { it.type == MOVIE_WATCHLIST.slug }.distinctBy { it.idTrakt }

    val request = SyncExportRequest(
      shows = exportShows.map { SyncExportItem.create(it.idTrakt, dateIsoStringFromMillis(it.updatedAt)) },
      movies = exportMovies.map { SyncExportItem.create(it.idTrakt, dateIsoStringFromMillis(it.updatedAt)) }
    )

    transactions.withTransaction {
      val ids = items.map { it.idTrakt }
      localSource.traktSyncQueue.deleteAll(ids, MOVIE_WATCHLIST.slug)
      localSource.traktSyncQueue.deleteAll(ids, SHOW_WATCHLIST.slug)
    }
    remoteSource.trakt.postSyncWatchlist(request)

    val currentCount = count + exportShows.count() + exportMovies.count()

    // Check for more items
    val newItems = localSource.traktSyncQueue.getAll(types.map { it.slug })
    if (newItems.isNotEmpty()) {
      delay(DELAY)
      return exportWatchlistItems(moviesEnabled, currentCount)
    }

    return currentCount
  }

  private suspend fun exportHiddenItems(
    moviesEnabled: Boolean,
    count: Int = 0
  ): Int {
    val types = if (moviesEnabled) listOf(HIDDEN_SHOW, HIDDEN_MOVIE) else listOf(HIDDEN_SHOW)
    val items = localSource.traktSyncQueue.getAll(types.map { it.slug }).take(BATCH_LIMIT)
    if (items.isEmpty()) {
      Timber.d("Nothing to export. Cancelling..")
      return count
    }

    Timber.d("Exporting hidden items...")

    val exportShows = items.filter { it.type == HIDDEN_SHOW.slug }.distinctBy { it.idTrakt }
    val exportMovies = items.filter { it.type == HIDDEN_MOVIE.slug }.distinctBy { it.idTrakt }

    transactions.withTransaction {
      val ids = items.map { it.idTrakt }
      with(localSource.traktSyncQueue) {
        deleteAll(ids, HIDDEN_SHOW.slug)
        deleteAll(ids, HIDDEN_MOVIE.slug)
      }
    }

    if (exportShows.isNotEmpty()) {
      remoteSource.trakt.postHiddenShows(
        shows = exportShows.map { SyncExportItem.create(it.idTrakt, hiddenAt = dateIsoStringFromMillis(it.updatedAt)) }
      )
      delay(1500)
    }

    if (exportMovies.isNotEmpty()) {
      remoteSource.trakt.postHiddenMovies(
        movies = exportMovies.map { SyncExportItem.create(it.idTrakt, hiddenAt = dateIsoStringFromMillis(it.updatedAt)) }
      )
    }

    val currentCount = count + exportShows.count() + exportMovies.count()

    // Check for more items
    val newItems = localSource.traktSyncQueue.getAll(types.map { it.slug })
    if (newItems.isNotEmpty()) {
      delay(DELAY)
      return exportHiddenItems(moviesEnabled, currentCount)
    }

    return currentCount
  }
}
