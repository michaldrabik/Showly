package com.michaldrabik.ui_discover.cases

import com.michaldrabik.common.Config
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_discover.recycler.DiscoverListItem
import com.michaldrabik.ui_model.DiscoverFilters
import com.michaldrabik.ui_model.DiscoverSortOrder
import com.michaldrabik.ui_model.DiscoverSortOrder.HOT
import com.michaldrabik.ui_model.DiscoverSortOrder.NEWEST
import com.michaldrabik.ui_model.DiscoverSortOrder.RATING
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.TranslationsRepository
import com.michaldrabik.ui_repository.UserTvdbManager
import com.michaldrabik.ui_repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
class DiscoverShowsCase @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val tvdbUserManager: UserTvdbManager,
  private val imagesProvider: ShowImagesProvider,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository
) {

  suspend fun isCacheValid() = showsRepository.discoverShows.isCacheValid()

  suspend fun loadCachedShows(filters: DiscoverFilters): List<DiscoverListItem> {
    val myShowsIds = showsRepository.myShows.loadAllIds()
    val watchlistShowsIds = showsRepository.watchlistShows.loadAllIds()
    val archiveShowsIds = showsRepository.archiveShows.loadAllIds()
    val cachedShows = showsRepository.discoverShows.loadAllCached()
    val language = settingsRepository.getLanguage()

    return prepareItems(
      cachedShows,
      myShowsIds,
      watchlistShowsIds,
      archiveShowsIds,
      filters,
      language
    )
  }

  suspend fun loadRemoteShows(filters: DiscoverFilters): List<DiscoverListItem> {
    val showAnticipated = !filters.hideAnticipated
    val genres = filters.genres.toList()

    try {
      tvdbUserManager.checkAuthorization()
    } catch (t: Throwable) {
      // Ignore at this moment
    }

    val myShowsIds = showsRepository.myShows.loadAllIds()
    val watchlistShowsIds = showsRepository.watchlistShows.loadAllIds()
    val archiveShowsIds = showsRepository.archiveShows.loadAllIds()
    val remoteShows = showsRepository.discoverShows.loadAllRemote(showAnticipated, genres)
    val language = settingsRepository.getLanguage()

    showsRepository.discoverShows.cacheDiscoverShows(remoteShows)
    return prepareItems(remoteShows, myShowsIds, watchlistShowsIds, archiveShowsIds, filters, language)
  }

  private suspend fun prepareItems(
    shows: List<Show>,
    myShowsIds: List<Long>,
    watchlistShowsIds: List<Long>,
    archiveShowsIds: List<Long>,
    filters: DiscoverFilters?,
    language: String
  ) = shows
    .filter { !archiveShowsIds.contains(it.traktId) }
    .sortedBy(filters?.feedOrder ?: HOT)
    .mapIndexed { index, show ->
      val itemType = when (index) {
        in (0..500 step 14) -> ImageType.FANART_WIDE
        in (5..500 step 14), in (9..500 step 14) -> ImageType.FANART
        else -> ImageType.POSTER
      }
      val image = imagesProvider.findCachedImage(show, itemType)
      val translation = loadTranslation(language, itemType, show)
      DiscoverListItem(
        show,
        image,
        isFollowed = show.ids.trakt.id in myShowsIds,
        isWatchlist = show.ids.trakt.id in watchlistShowsIds,
        translation = translation
      )
    }

  private suspend fun loadTranslation(language: String, itemType: ImageType, show: Show) =
    if (language == Config.DEFAULT_LANGUAGE || itemType == ImageType.POSTER) null
    else translationsRepository.loadTranslation(show, language, true)

  private fun List<Show>.sortedBy(order: DiscoverSortOrder) = when (order) {
    HOT -> this
    RATING -> this.sortedWith(compareByDescending<Show> { it.votes }.thenBy { it.rating })
    NEWEST -> this.sortedByDescending { it.year }
  }
}
