package com.michaldrabik.ui_episodes.details

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.michaldrabik.common.Config
import com.michaldrabik.common.Config.IMAGE_FADE_DURATION_MS
import com.michaldrabik.common.Config.SPOILERS_HIDE_SYMBOL
import com.michaldrabik.common.Config.SPOILERS_REGEX
import com.michaldrabik.common.extensions.dateFromMillis
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.common.sheets.ratings.RatingsBottomSheet
import com.michaldrabik.ui_base.common.sheets.ratings.RatingsBottomSheet.Options.Operation
import com.michaldrabik.ui_base.common.sheets.ratings.RatingsBottomSheet.Options.Type
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.invisible
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.optionalParcelable
import com.michaldrabik.ui_base.utilities.extensions.requireParcelable
import com.michaldrabik.ui_base.utilities.extensions.setTextFade
import com.michaldrabik.ui_base.utilities.extensions.showErrorSnackbar
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_comments.CommentView
import com.michaldrabik.ui_episodes.R
import com.michaldrabik.ui_episodes.databinding.ViewEpisodeDetailsBinding
import com.michaldrabik.ui_episodes.details.links.EpisodeLinksBottomSheet
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdTmdb
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.SpoilersSettings
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_navigation.java.NavigationArgs
import com.michaldrabik.ui_navigation.java.NavigationArgs.ACTION_EPISODE_TAB_SELECTED
import com.michaldrabik.ui_navigation.java.NavigationArgs.ACTION_NEW_COMMENT
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_COMMENT
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_COMMENT_ACTION
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_COMMENT_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_EPISODE_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_OPTIONS
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_REPLY_USER
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_COMMENT
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_EPISODE_DETAILS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale.ENGLISH

@AndroidEntryPoint
class EpisodeDetailsBottomSheet : BaseBottomSheetFragment(R.layout.view_episode_details) {

  companion object {
    fun createBundle(
      ids: Ids,
      episode: Episode,
      seasonEpisodesIds: List<Int>?,
      isWatched: Boolean,
      showTabs: Boolean,
    ): Bundle {
      val options = Options(
        ids = ids,
        episode = episode,
        seasonEpisodesIds = seasonEpisodesIds,
        isWatched = isWatched,
        showTabs = showTabs,
      )
      return bundleOf(ARG_OPTIONS to options)
    }
  }

  private val viewModel by viewModels<EpisodeDetailsViewModel>()
  private val binding by viewBinding(ViewEpisodeDetailsBinding::bind)

  private val options by lazy { requireParcelable<Options>(ARG_OPTIONS) }
  private val cornerRadius by lazy { dimenToPx(R.dimen.bottomSheetCorner).toFloat() }

  private var spoilerTitle: String? = null
  private var spoilerDescription: String? = null
  private var spoilerRating: String? = null

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    setupView()

    with(viewModel) {
      launchAndRepeatStarted(
        { uiState.collect { render(it) } },
        { messageFlow.collect { renderSnackbar(it) } },
        doAfterLaunch = {
          val (ids, episode, seasonEpisodes) = options
          loadLastWatchedAt(ids.trakt, episode)
          loadSeason(ids.trakt, episode, seasonEpisodes?.toIntArray())
          loadImage(ids.tmdb, episode)
          loadTranslation(ids.trakt, episode)
          loadRatings(episode)
        },
      )
    }
  }

  private fun setupView() {
    binding.run {
      val (ids, episode, _, isWatched, showTabs) = options
      episodeDetailsTitle.text = when (episode.title) {
        "Episode ${episode.number}" -> String.format(
          ENGLISH,
          requireContext().getString(R.string.textEpisode),
          episode.number,
        )
        else -> episode.title
      }
      episodeDetailsOverview.text = episode.overview.ifBlank { getString(R.string.textNoDescription) }
      episodeDetailsRatingLayout.visibleIf(episode.votes > 0)
      episodeDetailsWatchedAt.visibleIf(episode.lastWatchedAt != null || isWatched)
      if (!showTabs) episodeDetailsTabs.gone()
      episodeDetailsRating.text = String.format(ENGLISH, getString(R.string.textVotes), episode.rating, episode.votes)
      episodeDetailsCommentsButton.text = String.format(
        ENGLISH,
        getString(R.string.textLoadCommentsCount),
        episode.commentCount,
      )
      episodeDetailsCommentsButton.onClick {
        viewModel.loadComments(ids.trakt, episode.season, episode.number)
      }
      episodeDetailsPostCommentButton.onClick { openPostCommentSheet() }
      episodeDetailsLink.onClick { openEpisodeImdbLink(ids.tmdb,episode) }
    }
  }

  private fun openEpisodeImdbLink(idTmdb: IdTmdb, episode: Episode) {
    val bundle = EpisodeLinksBottomSheet.createBundle(idTmdb, episode)
    navigateTo(R.id.actionEpisodeDetailsDialogToLink, bundle)
  }

  private fun openRateDialog() {
    setFragmentResultListener(NavigationArgs.REQUEST_RATING) { _, bundle ->
      when (bundle.optionalParcelable<Operation>(NavigationArgs.RESULT)) {
        Operation.SAVE -> renderSnackbar(MessageEvent.Info(R.string.textRateSaved))
        Operation.REMOVE -> renderSnackbar(MessageEvent.Info(R.string.textRateRemoved))
        else -> Timber.w("Unknown result.")
      }
      viewModel.loadRatings(options.episode)
      setFragmentResult(REQUEST_EPISODE_DETAILS, bundleOf(NavigationArgs.ACTION_RATING_CHANGED to true))
    }
    val bundle = RatingsBottomSheet.createBundle(
      id = options.episode.ids.trakt,
      type = Type.EPISODE,
      seasonNumber = options.episode.season,
      episodeNumber = options.episode.number,
    )
    navigateTo(R.id.actionEpisodeDetailsDialogToRate, bundle)
  }

  private fun openPostCommentSheet(comment: Comment? = null) {
    setFragmentResultListener(REQUEST_COMMENT) { _, bundle ->
      renderSnackbar(MessageEvent.Info(R.string.textCommentPosted))
      when (bundle.getString(ARG_COMMENT_ACTION)) {
        ACTION_NEW_COMMENT -> {
          val newComment = bundle.getParcelable<Comment>(ARG_COMMENT)!!
          viewModel.addNewComment(newComment)
        }
      }
    }
    val bundle = when {
      comment != null -> bundleOf(
        ARG_COMMENT_ID to comment.getReplyId(),
        ARG_REPLY_USER to comment.user.username,
      )
      else -> bundleOf(ARG_EPISODE_ID to options.episode.ids.trakt.id)
    }
    navigateTo(R.id.actionEpisodeDetailsDialogToPostComment, bundle)
  }

  @SuppressLint("SetTextI18n")
  private fun render(uiState: EpisodeDetailsUiState) {
    uiState.run {
      with(binding) {
        val episode = options.episode
        dateFormat?.let {
          val millis = episode.firstAired?.toInstant()?.toEpochMilli() ?: -1
          val date = if (millis == -1L) {
            getString(R.string.textTba)
          } else {
            it.format(dateFromMillis(millis).toLocalZone()).capitalizeWords()
          }
          val name = String.format(
            ENGLISH,
            getString(R.string.textSeasonEpisodeDate),
            episode.season,
            episode.number,
            date,
          )
          val runtime = "${episode.runtime} ${getString(R.string.textMinutesShort)}"
          episodeDetailsName.text = if (episode.runtime > 0) "$name | $runtime" else name
        }
        isImageLoading.let { episodeDetailsProgress.visibleIf(it) }
        image?.let { renderImage(it, spoilers) }
        episodes?.let { renderEpisodes(it) }
        comments?.let { comments ->
          episodeDetailsComments.removeAllViews()
          comments.forEach {
            val view = CommentView(requireContext()).apply {
              bind(it, commentsDateFormat)
              if (it.replies > 0) {
                onRepliesClickListener = { comment -> viewModel.loadCommentReplies(comment) }
              }
              if (it.isSignedIn) {
                onReplyClickListener = { comment -> openPostCommentSheet(comment) }
              }
              if (it.replies == 0L && it.isMe && it.isSignedIn) {
                onDeleteClickListener = { comment -> openDeleteCommentDialog(comment) }
              }
            }
            episodeDetailsComments.addView(view)
          }
          episodeDetailsComments.fadeIf(comments.isNotEmpty())
          episodeDetailsCommentsEmpty.fadeIf(comments.isEmpty())
          episodeDetailsPostCommentButton.fadeIf(isSignedIn)
          episodeDetailsCommentsButton.isEnabled = false
          episodeDetailsCommentsButton.text = String.format(
            ENGLISH,
            getString(R.string.textLoadCommentsCount),
            comments.size,
          )
        }
        rating?.let { state ->
          episodeDetailsRateProgress.visibleIf(state.rateLoading == true)
          episodeDetailsRateButton.visibleIf(state.rateLoading == false, gone = false)
          episodeDetailsRateButton.isEnabled = state.rateLoading == false
          episodeDetailsRateButton.onClick { openRateDialog() }
          if (state.hasRating()) {
            episodeDetailsRateButton.text = "${state.userRating?.rating} / 10"
          } else {
            episodeDetailsRateButton.setText(R.string.textRate)
          }
        }
        spoilers?.let { renderRating(it) }
        isCommentsLoading.let {
          episodeDetailsCommentsProgress.visibleIf(it)
          episodeDetailsCommentsButton.visibleIf(!it, gone = false)
          episodeDetailsCommentsButton.isEnabled = !it
          episodeDetailsRateButton.isEnabled = !it
        }
        renderTitle(translation, spoilers)
        renderDescription(translation, spoilers)
        renderWatchedAt(lastWatchedAt, dateFormat)
      }
    }
  }

  private fun renderTitle(
    translation: Translation?,
    spoilersSettings: SpoilersSettings?,
  ) {
    with(binding) {
      var title =
        if (translation?.title?.isNotBlank() == true) {
          translation.title
        } else if (episodeDetailsTitle.text.isBlank()) {
          when (options.episode.title) {
            "Episode ${options.episode.number}" ->
              String.format(ENGLISH, requireContext().getString(R.string.textEpisode), options.episode.number)
            else -> options.episode.title
          }
        } else {
          episodeDetailsTitle.text.toString()
        }

      val isEpisodeTitleHidden = !options.isWatched && spoilersSettings?.isEpisodeTitleHidden == true
      if (isEpisodeTitleHidden) {
        if (title.any { it.isLetter() }) {
          spoilerTitle = String(title.toCharArray())
        }
        title = SPOILERS_REGEX.replace(title, SPOILERS_HIDE_SYMBOL)
      }

      if (title.isNotBlank()) {
        episodeDetailsTitle.setTextFade(title, duration = 0)
      }

      if (spoilersSettings?.isTapToReveal == true) {
        episodeDetailsTitle.onClick {
          spoilerTitle?.let {
            episodeDetailsTitle.setTextFade(it, duration = 0)
          }
        }
      }
    }
  }

  private fun renderDescription(
    translation: Translation?,
    spoilersSettings: SpoilersSettings?,
  ) {
    with(binding) {
      var description =
        if (translation?.overview?.isNotBlank() == true) {
          translation.overview
        } else if (episodeDetailsOverview.text.isBlank()) {
          options.episode.overview.ifBlank {
            getString(R.string.textNoDescription)
          }
        } else {
          episodeDetailsOverview.text.toString()
        }

      if (!options.isWatched && spoilersSettings?.isEpisodeDescriptionHidden == true) {
        if (description.any { it.isLetter() }) {
          spoilerDescription = String(description.toCharArray())
        }
        description = SPOILERS_REGEX.replace(description, SPOILERS_HIDE_SYMBOL)
      }

      if (description.isNotBlank()) {
        episodeDetailsOverview.setTextFade(description, duration = 0)
      }

      if (spoilersSettings?.isTapToReveal == true) {
        episodeDetailsOverview.onClick {
          spoilerDescription?.let {
            episodeDetailsOverview.setTextFade(it, duration = 0)
          }
        }
      }
    }
  }

  private fun renderWatchedAt(
    watchedAt: ZonedDateTime?,
    dateFormat: DateTimeFormatter?,
  ) {
    with(binding) {
      dateFormat?.let {
        episodeDetailsWatchedAt.visibleIf(watchedAt != null)
        episodeDetailsWatchedAt.text = watchedAt?.toLocalZone()?.format(it)
      }
    }
  }

  private fun renderRating(spoilersSettings: SpoilersSettings) {
    with(binding) {
      val isSpoilerHidden = !options.isWatched && spoilersSettings.isEpisodeRatingHidden
      if (isSpoilerHidden) {
        if (spoilerRating == null) {
          spoilerRating = episodeDetailsRating.text.toString()
        }
        episodeDetailsRating.text = Config.SPOILERS_RATINGS_VOTES_HIDE_SYMBOL
      }

      if (spoilersSettings.isTapToReveal) {
        episodeDetailsRating.onClick {
          spoilerRating?.let {
            episodeDetailsRating.text = it
          }
        }
      }
    }
  }

  private fun renderImage(
    image: Image,
    spoilers: SpoilersSettings?,
    tapToReveal: Boolean = false,
  ) {
    with(binding) {
      if (!options.isWatched && spoilers?.isEpisodeImageHidden == true && !tapToReveal) {
        episodeDetailsImage.invisible()
        episodeDetailsImagePlaceholder.visible()
        episodeDetailsImagePlaceholder.setImageResource(R.drawable.ic_eye_no)
        if (spoilers.isTapToReveal) {
          episodeDetailsImagePlaceholder.onClick {
            renderImage(image, spoilers, tapToReveal = true)
          }
        }
        return
      }
      episodeDetailsImage.visible()
      episodeDetailsImagePlaceholder.invisible()
      Glide
        .with(this@EpisodeDetailsBottomSheet)
        .load("${Config.TMDB_IMAGE_BASE_STILL_URL}${image.fileUrl}")
        .transform(CenterCrop(), GranularRoundedCorners(cornerRadius, cornerRadius, 0F, 0F))
        .transition(DrawableTransitionOptions.withCrossFade(IMAGE_FADE_DURATION_MS))
        .withFailListener {
          episodeDetailsImagePlaceholder.visible()
          episodeDetailsImagePlaceholder.setImageResource(R.drawable.ic_television)
          episodeDetailsImagePlaceholder.setOnClickListener(null)
        }.into(episodeDetailsImage)
    }
  }

  private fun renderEpisodes(episodes: List<Episode>) {
    with(binding.episodeDetailsTabs) {
      removeAllTabs()
      removeOnTabSelectedListener(tabSelectedListener)
      episodes.forEach {
        addTab(
          newTab()
            .setText("${options.episode.season}x${it.number.toString().padStart(2, '0')}")
            .setTag(it),
        )
      }
      val index = episodes.indexOfFirst { it.number == options.episode.number }
      // Small trick to avoid UI tab change flick
      getTabAt(index)?.select()
      post {
        getTabAt(index)?.select()
        addOnTabSelectedListener(tabSelectedListener)
      }
      if (options.showTabs && episodes.isNotEmpty()) {
        fadeIn(duration = 200, startDelay = 100, withHardware = true)
      } else {
        gone()
      }
    }
  }

  private fun renderSnackbar(message: MessageEvent) {
    when (message) {
      is MessageEvent.Info -> binding.episodeDetailsSnackbarHost.showInfoSnackbar(getString(message.textRestId))
      is MessageEvent.Error -> binding.episodeDetailsSnackbarHost.showErrorSnackbar(getString(message.textRestId))
    }
  }

  private fun openDeleteCommentDialog(comment: Comment) {
    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setTitle(R.string.textCommentConfirmDeleteTitle)
      .setMessage(R.string.textCommentConfirmDelete)
      .setPositiveButton(R.string.textYes) { _, _ -> viewModel.deleteComment(comment) }
      .setNegativeButton(R.string.textNo) { _, _ -> }
      .show()
  }

  private val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
    override fun onTabSelected(tab: TabLayout.Tab?) {
      binding.episodeDetailsTabs.removeOnTabSelectedListener(this)
      closeSheet()
      setFragmentResult(REQUEST_EPISODE_DETAILS, bundleOf(ACTION_EPISODE_TAB_SELECTED to tab?.tag))
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) = Unit

    override fun onTabReselected(tab: TabLayout.Tab?) = Unit
  }

  @Parcelize
  private data class Options(
    val ids: Ids,
    val episode: Episode,
    val seasonEpisodesIds: List<Int>?,
    val isWatched: Boolean,
    val showTabs: Boolean,
  ) : Parcelable
}
