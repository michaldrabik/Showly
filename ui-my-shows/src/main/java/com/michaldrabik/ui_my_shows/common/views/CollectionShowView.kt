package com.michaldrabik.ui_my_shows.common.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.common.Config
import com.michaldrabik.common.Config.SPOILERS_HIDE_SYMBOL
import com.michaldrabik.common.Config.SPOILERS_REGEX
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.common.recycler.CollectionListItem
import com.michaldrabik.ui_my_shows.databinding.ViewCollectionShowBinding
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n")
class CollectionShowView : ShowView<CollectionListItem.ShowItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewCollectionShowBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    with(binding) {
      collectionShowRoot.onClick { itemClickListener?.invoke(item) }
      collectionShowRoot.onLongClick { itemLongClickListener?.invoke(item) }
    }
    imageLoadCompleteListener = { loadTranslation() }
  }

  override val imageView: ImageView = binding.collectionShowImage
  override val placeholderView: ImageView = binding.collectionShowPlaceholder

  private var nowUtc = nowUtc()

  private lateinit var item: CollectionListItem.ShowItem

  override fun bind(item: CollectionListItem.ShowItem) {
    clear()
    this.item = item
    with(binding) {
      collectionShowProgress.visibleIf(item.isLoading)
      collectionShowTitle.text =
        if (item.translation?.title.isNullOrBlank()) item.show.title
        else item.translation?.title

      bindDescription(item)
      bindRating(item)

      collectionShowNetwork.text =
        if (item.show.year > 0) context.getString(R.string.textNetwork, item.show.network, item.show.year.toString())
        else String.format("%s", item.show.network)

      collectionShowNetwork.visibleIf(item.show.network.isNotBlank())

      with(collectionShowReleaseDate) {
        val releaseDate = item.getReleaseDate()
        if (releaseDate != null) {
          visibleIf(releaseDate.isAfter(nowUtc))
          text = item.dateFormat.format(releaseDate).capitalizeWords()
        } else {
          gone()
        }
      }

      item.userRating?.let {
        collectionShowUserStarIcon.visible()
        collectionShowUserRating.visible()
        collectionShowUserRating.text = String.format(ENGLISH, "%d", it)
      }
    }

    loadImage(item)
  }

  private fun bindDescription(item: CollectionListItem.ShowItem) {
    with(binding) {
      collectionShowDescription.text =
        if (item.translation?.overview.isNullOrBlank()) item.show.overview
        else item.translation?.overview

      if (item.spoilers.isSpoilerHidden) {
        collectionShowDescription.tag = collectionShowDescription.text
        collectionShowDescription.text =
          SPOILERS_REGEX.replace(collectionShowDescription.text, SPOILERS_HIDE_SYMBOL)

        if (item.spoilers.isSpoilerTapToReveal) {
          collectionShowDescription.onClick { view ->
            view.tag?.let { collectionShowDescription.text = it.toString() }
            view.isClickable = false
          }
        }
      }

      collectionShowDescription.visibleIf(item.show.overview.isNotBlank())
    }
  }

  private fun bindRating(item: CollectionListItem.ShowItem) {
    var rating = String.format(ENGLISH, "%.1f", item.show.rating)

    with(binding) {
      if (item.spoilers.isSpoilerRatingsHidden) {
        collectionShowRating.tag = rating
        rating = Config.SPOILERS_RATINGS_HIDE_SYMBOL

        if (item.spoilers.isSpoilerTapToReveal) {
          collectionShowRating.onClick { view ->
            view.tag?.let { collectionShowRating.text = it.toString() }
            view.isClickable = false
          }
        }
      }

      collectionShowRating.text = rating
    }
  }

  private fun loadTranslation() {
    if (item.translation == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    with(binding) {
      collectionShowTitle.text = ""
      collectionShowDescription.text = ""
      collectionShowNetwork.text = ""
      collectionShowRating.text = ""
      collectionShowPlaceholder.gone()
      collectionShowUserRating.gone()
      collectionShowUserStarIcon.gone()
      Glide.with(this@CollectionShowView).clear(collectionShowImage)
    }
  }
}
