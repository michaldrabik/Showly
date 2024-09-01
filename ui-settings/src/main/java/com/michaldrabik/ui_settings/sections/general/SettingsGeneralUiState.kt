package com.michaldrabik.ui_settings.sections.general

import com.michaldrabik.common.Config
import com.michaldrabik.ui_model.locale.AppCountry
import com.michaldrabik.ui_base.dates.AppDateFormat
import com.michaldrabik.ui_model.ProgressDateSelectionType
import com.michaldrabik.ui_model.ProgressNextEpisodeType
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_model.locale.AppLanguage
import com.michaldrabik.ui_settings.helpers.AppTheme

data class SettingsGeneralUiState(
  val settings: Settings? = null,
  val isPremium: Boolean = false,
  val language: AppLanguage = AppLanguage.ENGLISH,
  val theme: AppTheme = AppTheme.DARK,
  val country: AppCountry? = null,
  val dateFormat: AppDateFormat? = null,
  val moviesEnabled: Boolean = true,
  val newsEnabled: Boolean = false,
  val streamingsEnabled: Boolean = true,
  val restartApp: Boolean = false,
  val progressNextType: ProgressNextEpisodeType? = null,
  val progressDateSelectionType: ProgressDateSelectionType? = null,
  val progressUpcomingDays: Long? = null,
  val tabletColumns: Int = Config.DEFAULT_LISTS_GRID_SPAN,
)
