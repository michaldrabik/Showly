package com.michaldrabik.ui_progress.recents.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.addRipple
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.expandTouch
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.recents.recycler.RecentsListItem
import kotlinx.android.synthetic.main.view_progress_recents_item.view.*
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n")
class ProgressRecentsItemView : ShowView<RecentsListItem.Episode> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var detailsClickListener: ((RecentsListItem) -> Unit)? = null

  init {
    inflate(context, R.layout.view_progress_recents_item, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    addRipple()

    onClick { itemClickListener?.invoke(item) }

    progressRecentsItemInfoButton.expandTouch(100)
    progressRecentsItemInfoButton.onClick { detailsClickListener?.invoke(item) }
    imageLoadCompleteListener = { loadTranslation() }
  }

  private lateinit var item: RecentsListItem.Episode

  override val imageView: ImageView = progressRecentsItemImage
  override val placeholderView: ImageView = progressRecentsItemPlaceholder

  override fun bind(item: RecentsListItem.Episode) {
    this.item = item
    clear()

    progressRecentsItemTitle.text =
      if (item.translations?.show?.title.isNullOrBlank()) item.show.title
      else item.translations?.show?.title

    progressRecentsItemDateText.text =
      item.episode.firstAired?.toLocalZone()?.let { item.dateFormat?.format(it)?.capitalizeWords() }

    val episodeTitle = when {
      item.episode.title.isBlank() -> context.getString(R.string.textTba)
      item.translations?.episode?.title?.isBlank() == false -> item.translations.episode.title
      else -> item.episode.title
    }
    progressRecentsItemSubtitle2.text = episodeTitle
    progressRecentsItemSubtitle.text = String.format(
      ENGLISH,
      context.getString(R.string.textSeasonEpisode),
      item.episode.season,
      item.episode.number
    )

    progressRecentsItemCheckButton.visibleIf(!item.isWatched)

    loadImage(item)
  }

  private fun loadTranslation() {
    if (item.translations?.show == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    progressRecentsItemPlaceholder.gone()
    Glide.with(this).clear(progressRecentsItemImage)
  }
}
