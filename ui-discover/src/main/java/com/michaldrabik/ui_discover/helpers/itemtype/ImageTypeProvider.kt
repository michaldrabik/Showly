package com.michaldrabik.ui_discover.helpers.itemtype

import com.michaldrabik.ui_model.ImageType

internal interface ImageTypeProvider {
  fun getImageType(position: Int): ImageType
}
