package com.michaldrabik.ui_my_shows.archive

import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_my_shows.archive.recycler.ArchiveListItem

data class ArchiveUiState(
  val items: List<ArchiveListItem> = emptyList(),
  val resetScroll: Event<Boolean>? = null,
  val sortOrder: Event<Pair<SortOrder, SortType>>? = null,
)
