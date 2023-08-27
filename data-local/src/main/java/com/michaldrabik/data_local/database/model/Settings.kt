package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
  @PrimaryKey @ColumnInfo(name = "id") val id: Long = 1,
  @ColumnInfo(name = "is_initial_run", defaultValue = "0") val isInitialRun: Boolean,
  @ColumnInfo(name = "push_notifications_enabled", defaultValue = "1") val pushNotificationsEnabled: Boolean, // Removed
  @ColumnInfo(name = "episodes_notifications_enabled", defaultValue = "1") val episodesNotificationsEnabled: Boolean,
  @ColumnInfo(name = "episodes_notifications_delay", defaultValue = "0") val episodesNotificationsDelay: Long,
  @ColumnInfo(name = "my_shows_recent_amount", defaultValue = "6") val myShowsRecentsAmount: Int,
  @ColumnInfo(name = "my_shows_running_sort_by", defaultValue = "NAME") val myShowsRunningSortBy: String,
  @ColumnInfo(name = "my_shows_incoming_sort_by", defaultValue = "NAME") val myShowsIncomingSortBy: String,
  @ColumnInfo(name = "my_shows_ended_sort_by", defaultValue = "NAME") val myShowsEndedSortBy: String,
  @ColumnInfo(name = "my_shows_all_sort_by", defaultValue = "NAME") val myShowsAllSortBy: String,
  @ColumnInfo(name = "my_shows_running_is_collapsed", defaultValue = "0") val myShowsRunningIsCollapsed: Boolean,
  @ColumnInfo(name = "my_shows_incoming_is_collapsed", defaultValue = "0") val myShowsIncomingIsCollapsed: Boolean,
  @ColumnInfo(name = "my_shows_ended_is_collapsed", defaultValue = "0") val myShowsEndedIsCollapsed: Boolean,
  @ColumnInfo(name = "my_shows_running_is_enabled", defaultValue = "1") val myShowsRunningIsEnabled: Boolean,
  @ColumnInfo(name = "my_shows_incoming_is_enabled", defaultValue = "1") val myShowsIncomingIsEnabled: Boolean,
  @ColumnInfo(name = "my_shows_ended_is_enabled", defaultValue = "1") val myShowsEndedIsEnabled: Boolean,
  @ColumnInfo(name = "my_shows_recent_is_enabled", defaultValue = "1") val myShowsRecentIsEnabled: Boolean,
  @ColumnInfo(name = "see_later_shows_sort_by", defaultValue = "NAME") val seeLaterShowsSortBy: String,
  @ColumnInfo(name = "show_anticipated_shows", defaultValue = "1") val showAnticipatedShows: Boolean,
  @ColumnInfo(name = "discover_filter_genres", defaultValue = "") val discoverFilterGenres: String,
  @ColumnInfo(name = "discover_filter_networks", defaultValue = "") val discoverFilterNetworks: String,
  @ColumnInfo(name = "discover_filter_feed", defaultValue = "HOT") val discoverFilterFeed: String,
  @ColumnInfo(name = "trakt_sync_schedule", defaultValue = "OFF") val traktSyncSchedule: String,
  @ColumnInfo(name = "trakt_quick_sync_enabled", defaultValue = "0") val traktQuickSyncEnabled: Boolean,
  @ColumnInfo(name = "trakt_quick_remove_enabled", defaultValue = "0") val traktQuickRemoveEnabled: Boolean,
  @ColumnInfo(name = "watchlist_sort_by", defaultValue = "NAME") val watchlistSortBy: String,
  @ColumnInfo(name = "archive_shows_sort_by", defaultValue = "NAME") val archiveShowsSortBy: String,
  @ColumnInfo(name = "archive_shows_include_statistics", defaultValue = "1") val archiveShowsIncludeStatistics: Boolean,
  @ColumnInfo(name = "special_seasons_enabled", defaultValue = "0") val specialSeasonsEnabled: Boolean,
  @ColumnInfo(name = "show_anticipated_movies", defaultValue = "0") val showAnticipatedMovies: Boolean,
  @ColumnInfo(name = "discover_movies_filter_genres", defaultValue = "") val discoverMoviesFilterGenres: String,
  @ColumnInfo(name = "discover_movies_filter_feed", defaultValue = "HOT") val discoverMoviesFilterFeed: String,
  @ColumnInfo(name = "my_movies_all_sort_by", defaultValue = "NAME") val myMoviesAllSortBy: String,
  @ColumnInfo(name = "see_later_movies_sort_by", defaultValue = "NAME") val seeLaterMoviesSortBy: String,
  @ColumnInfo(name = "progress_movies_sort_by", defaultValue = "NAME") val progressMoviesSortBy: String,
  @ColumnInfo(name = "show_collection_shows", defaultValue = "1") val showCollectionShows: Boolean,
  @ColumnInfo(name = "show_collection_movies", defaultValue = "1") val showCollectionMovies: Boolean,
  @ColumnInfo(name = "widgets_show_label", defaultValue = "1") val widgetsShowLabel: Boolean,
  @ColumnInfo(name = "my_movies_recent_is_enabled", defaultValue = "1") val myMoviesRecentIsEnabled: Boolean,
  @ColumnInfo(name = "quick_rate_enabled", defaultValue = "0") val quickRateEnabled: Boolean,
  @ColumnInfo(name = "lists_sort_by", defaultValue = "DATE_UPDATED") val listsSortBy: String,
  @ColumnInfo(name = "progress_upcoming_enabled", defaultValue = "1") val progressUpcomingEnabled: Boolean,
)
