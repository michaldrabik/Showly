package com.michaldrabik.showly2.ui.watchlist

import com.michaldrabik.showly2.ui.common.UiModel

data class WatchlistUiModel(
  val info: Int? = null,
  val error: Error? = null
) : UiModel() {

  override fun update(newModel: UiModel): UiModel {
    return super.update(newModel)
  }

}