package com.michaldrabik.ui_progress.progress.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.michaldrabik.common.Config.SPOILERS_HIDE_SYMBOL
import com.michaldrabik.common.Config.SPOILERS_RATINGS_HIDE_SYMBOL
import com.michaldrabik.common.Config.SPOILERS_REGEX
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.DurationPrinter
import com.michaldrabik.ui_base.utilities.extensions.addRipple
import com.michaldrabik.ui_base.utilities.extensions.bump
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.colorStateListFromAttr
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.expandTouch
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_model.SortOrder.USER_RATING
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.databinding.ViewProgressItemBinding
import com.michaldrabik.ui_progress.progress.recycler.ProgressListItem
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n")
class ProgressItemView : ShowView<ProgressListItem.Episode> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewProgressItemBinding.inflate(LayoutInflater.from(context), this)

  var detailsClickListener: ((ProgressListItem.Episode) -> Unit)? = null
  var checkClickListener: ((ProgressListItem.Episode) -> Unit)? = null

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    addRipple()
    binding.progressItemCheckButton.expandTouch(100)

    onClick { itemClickListener?.invoke(item) }
    onLongClick { itemLongClickListener?.invoke(item) }
    binding.progressItemInfoButton.onClick { detailsClickListener?.invoke(item) }
    imageLoadCompleteListener = { loadTranslation() }
  }

  private lateinit var item: ProgressListItem.Episode

  override val imageView: ImageView = binding.progressItemImage
  override val placeholderView: ImageView = binding.progressItemPlaceholder

  private val durationPrinter by lazy { DurationPrinter(context.applicationContext) }
  private val checkButtonWidth by lazy { context.dimenToPx(R.dimen.progressItemCheckButtonWidth) }
  private val checkButtonHeight by lazy { context.dimenToPx(R.dimen.progressItemButtonHeight) }

  override fun bind(item: ProgressListItem.Episode) {
    this.item = item
    clear()

    with(binding) {
      val translationTitle = item.translations?.show?.title
      progressItemTitle.text =
        if (translationTitle.isNullOrBlank()) item.show.title
        else translationTitle

      progressItemSubtitle.text = String.format(
        ENGLISH,
        context.getString(R.string.textSeasonEpisode),
        item.episode?.season,
        item.episode?.number
      ).plus(
        item.episode?.numberAbs?.let { if (it > 0 && item.show.isAnime) " ($it)" else "" } ?: ""
      )

      bindEpisodeTitle(item)
      progressItemNewBadge.visibleIf(item.isNew())
      progressItemPin.visibleIf(item.isPinned)
      progressItemPause.visibleIf(item.isOnHold)
    }

    bindProgress(item)
    bindRating(item)
    bindCheckButton(item, checkClickListener, detailsClickListener)

    loadImage(item)
  }

  private fun bindEpisodeTitle(item: ProgressListItem.Episode) {
    var episodeTitle = when {
      item.episode?.title?.isBlank() == true -> {
        context.getString(R.string.textTba)
      }
      item.translations?.episode?.title?.isBlank() == false -> {
        item.translations.episode.title
      }
      item.episode?.title == "Episode ${item.episode?.number}" -> {
        String.format(ENGLISH, context.getString(R.string.textEpisode), item.episode.number)
      }
      else -> item.episode?.title
    }

    if (item.spoilers?.isEpisodeTitleHidden == true) {
      with(binding) {
        progressItemSubtitle2.tag = episodeTitle
        episodeTitle = SPOILERS_REGEX.replace(episodeTitle.toString(), SPOILERS_HIDE_SYMBOL)

        if (item.spoilers.isTapToReveal) {
          progressItemSubtitle2.onClick { view ->
            view.tag?.let { progressItemSubtitle2.text = it.toString() }
            view.isClickable = false
          }
        }
      }
    }

    binding.progressItemSubtitle2.text = episodeTitle
  }

  private fun bindRating(episodeItem: ProgressListItem.Episode) {
    val isNew = episodeItem.isNew()
    val isUpcoming = episodeItem.isUpcoming
    with(binding) {
      when (episodeItem.sortOrder) {
        RATING -> {
          progressItemRating.visibleIf(!isNew && !isUpcoming)
          progressItemRatingStar.visibleIf(!isNew && !isUpcoming)
          progressItemRatingStar.imageTintList = context.colorStateListFromAttr(android.R.attr.colorAccent)
          val rating = String.format(ENGLISH, "%.1f", episodeItem.show.rating)
          if (item.spoilers?.isMyShowsRatingsHidden == true) {
            progressItemRating.tag = rating
            progressItemRating.text = SPOILERS_RATINGS_HIDE_SYMBOL
            if (item.spoilers?.isTapToReveal == true) {
              progressItemRating.onClick { view ->
                view.tag?.let {
                  progressItemRating.text = it.toString()
                }
                view.isClickable = false
              }
            }
          } else {
            progressItemRating.text = rating
          }
        }
        USER_RATING -> {
          val hasRating = episodeItem.userRating != null
          progressItemRating.visibleIf(!isNew && !isUpcoming && hasRating)
          progressItemRatingStar.visibleIf(!isNew && !isUpcoming && hasRating)
          progressItemRatingStar.imageTintList = context.colorStateListFromAttr(android.R.attr.textColorPrimary)
          progressItemRating.text = String.format(ENGLISH, "%d", episodeItem.userRating)
        }
        else -> {
          progressItemRating.gone()
          progressItemRatingStar.gone()
        }
      }
    }
  }

  private fun bindProgress(item: ProgressListItem.Episode) {
    binding.progressItemProgress.max = item.totalCount
    binding.progressItemProgress.progress = item.watchedCount
    renderEpisodesLeft()
  }

  private fun bindCheckButton(
    item: ProgressListItem.Episode,
    checkClickListener: ((ProgressListItem.Episode) -> Unit)?,
    detailsClickListener: ((ProgressListItem.Episode) -> Unit)?,
  ) {
    val hasAired = item.episode?.hasAired(item.season ?: Season.EMPTY) == true
    val color = if (hasAired) android.R.attr.textColorPrimary else android.R.attr.textColorSecondary

    with(binding) {
      if (hasAired) {
        progressItemInfoButton.visible()
        progressItemCheckButton.run {
          layoutParams = LinearLayout.LayoutParams(checkButtonWidth, checkButtonHeight)
          text = ""
          setIconResource(R.drawable.ic_check)
          onClick { it.bump { checkClickListener?.invoke(item) } }
        }
      } else {
        progressItemInfoButton.gone()
        progressItemCheckButton.run {
          layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, checkButtonHeight)
          text = durationPrinter.print(item.episode?.firstAired)
          icon = null
          onClick { detailsClickListener?.invoke(item) }
        }
      }
      progressItemCheckButton.setTextColor(context.colorFromAttr(color))
      progressItemCheckButton.strokeColor = context.colorStateListFromAttr(color)
      progressItemCheckButton.iconTint = context.colorStateListFromAttr(color)
    }
  }

  private fun loadTranslation() {
    if (item.translations?.show == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun renderEpisodesLeft() {
    with(binding) {
      val episodesLeft = item.totalCount - item.watchedCount
      if (episodesLeft <= 0) {
        progressItemProgressText.text = String.format(ENGLISH, "%d/%d", item.watchedCount, item.totalCount)
      } else {
        val episodesLeftString = resources.getQuantityString(R.plurals.textEpisodesLeft, episodesLeft, episodesLeft)
        progressItemProgressText.text = String.format(ENGLISH, "%d/%d ($episodesLeftString)", item.watchedCount, item.totalCount)
      }
    }
  }

  private fun clear() {
    with(binding) {
      progressItemTitle.text = ""
      progressItemSubtitle.text = ""
      progressItemSubtitle2.text = ""
      progressItemProgressText.text = ""
      progressItemPlaceholder.gone()
      Glide.with(this@ProgressItemView).clear(progressItemImage)
    }
  }
}
