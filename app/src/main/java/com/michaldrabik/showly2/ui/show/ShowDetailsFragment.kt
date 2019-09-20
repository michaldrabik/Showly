package com.michaldrabik.showly2.ui.show

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.michaldrabik.showly2.Config.TVDB_IMAGE_BASE_URL
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.Season
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.show.actors.ActorsAdapter
import com.michaldrabik.showly2.ui.show.related.RelatedShowAdapter
import com.michaldrabik.showly2.ui.show.seasons.SeasonsAdapter
import com.michaldrabik.showly2.utilities.extensions.*
import kotlinx.android.synthetic.main.fragment_show_details.*
import kotlinx.android.synthetic.main.fragment_show_details_next_episode.*
import org.threeten.bp.Duration

@SuppressLint("SetTextI18n", "DefaultLocale")
class ShowDetailsFragment : BaseFragment<ShowDetailsViewModel>() {

  companion object {
    const val ARG_SHOW_ID = "ARG_SHOW_ID"
  }

  override val layoutResId = R.layout.fragment_show_details

  private val showId by lazy { arguments?.getLong(ARG_SHOW_ID, -1) ?: -1 }

  private val actorsAdapter by lazy { ActorsAdapter() }
  private val relatedAdapter by lazy { RelatedShowAdapter() }
  private val seasonsAdapter by lazy { SeasonsAdapter() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    appComponent().inject(this)
  }

  override fun createViewModel() =
    ViewModelProvider(this, viewModelFactory).get(ShowDetailsViewModel::class.java)
      .apply {
        uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
      }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupActorsList()
    setupRelatedList()
    setupSeasonsList()
    viewModel.loadShowDetails(showId)
  }

  override fun onResume() {
    super.onResume()
    handleBackPressed()
  }

  private fun setupView() {
    showDetailsImageGuideline.setGuidelineBegin((screenHeight() * 0.33).toInt())
    showDetailsBackArrow.onClick { requireActivity().onBackPressed() }
  }

  private fun setupActorsList() {
    val context = requireContext()
    showDetailsActorsRecycler.apply {
      setHasFixedSize(true)
      adapter = actorsAdapter
      layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
      addItemDecoration(DividerItemDecoration(context, HORIZONTAL).apply {
        setDrawable(ContextCompat.getDrawable(context, R.drawable.divider_actors)!!)
      })
    }
    actorsAdapter.itemClickListener = {
      showDetailsRoot.showInfoSnackbar(getString(R.string.textActorRole, it.name, it.role))
    }
  }

  private fun setupRelatedList() {
    val context = requireContext()
    showDetailsRelatedRecycler.apply {
      setHasFixedSize(true)
      adapter = relatedAdapter
      layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
      addItemDecoration(DividerItemDecoration(context, HORIZONTAL).apply {
        setDrawable(ContextCompat.getDrawable(context, R.drawable.divider_related_shows)!!)
      })
    }
    relatedAdapter.missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) }
    relatedAdapter.itemClickListener = {
      val bundle = Bundle().apply { putLong(ARG_SHOW_ID, it.show.ids.trakt) }
      findNavController().navigate(R.id.actionShowDetailsFragmentToSelf, bundle)
    }
  }

  private fun setupSeasonsList() {
    val context = requireContext()
    showDetailsSeasonsRecycler.apply {
      setHasFixedSize(true)
      adapter = seasonsAdapter
      layoutManager = LinearLayoutManager(context, VERTICAL, false)
    }
    seasonsAdapter.itemClickListener = { showEpisodesView(it.season) }
  }

  private fun showEpisodesView(season: Season) {
    val animationEnter = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_slide_in_from_right)
    val animationExit = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_slide_out_from_right)

    showDetailsEpisodesView.run {
      bind(season)
      fadeIn(275) {
        bindEpisodes(season.episodes)
      }
      startAnimation(animationEnter)
    }

    showDetailsMainLayout.run {
      fadeOut()
      startAnimation(animationExit)
    }
  }

  private fun hideEpisodesView() {
    val animationEnter = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_slide_in_from_left)
    val animationExit = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_slide_out_from_left)

    showDetailsEpisodesView.run {
      fadeOut()
      startAnimation(animationExit)
    }

    showDetailsMainLayout.run {
      fadeIn()
      startAnimation(animationEnter)
    }
  }

  private fun render(uiModel: ShowDetailsUiModel) {
    uiModel.showLoading?.let {
      showDetailsMainLayout.fadeIf(!it)
      showDetailsMainProgress.visibleIf(it)
    }
    uiModel.show?.let { show ->
      showDetailsTitle.text = show.title
      showDetailsDescription.text = show.overview
      showDetailsStatus.text = show.status.split(" ").joinToString(" ") { it.capitalize() }
      showDetailsExtraInfo.text =
        "${show.network} ${show.year} | ${show.runtime} min | ${show.genres.take(2).joinToString(", ") { it.capitalize() }}"
      showDetailsRating.text = String.format("%.1f (%d votes)", show.rating, show.votes)
    }
    uiModel.nextEpisode?.let { renderNextEpisode(it) }
    uiModel.imageLoading?.let { showDetailsImageProgress.visibleIf(it) }
    uiModel.image?.let { renderImage(it) }
    uiModel.actors?.let {
      actorsAdapter.setItems(it)
      showDetailsActorsRecycler.fadeIf(it.isNotEmpty())
      showDetailsActorsProgress.gone()
    }
    uiModel.seasons?.let {
      seasonsAdapter.setItems(it)
      showDetailsSeasonsRecycler.fadeIf(it.isNotEmpty())
      showDetailsSeasonsLabel.fadeIf(it.isNotEmpty())
      separator2.fadeIf(it.isNotEmpty())
    }
    uiModel.relatedShows?.let {
      relatedAdapter.setItems(it)
      showDetailsRelatedRecycler.fadeIf(it.isNotEmpty())
      showDetailsRelatedLabel.fadeIf(it.isNotEmpty())
      separator3.fadeIf(it.isNotEmpty())
    }
    uiModel.updateRelatedShow?.let { relatedAdapter.updateItem(it) }
  }

  private fun renderNextEpisode(nextEpisode: Episode) {
    nextEpisode.run {
      showDetailsEpisodeText.text = "${toDisplayString()} - '$title'"
      showDetailsEpisodeCard.visible()

      val timeToAir = Duration.between(nowUtc(), firstAired)
      if (timeToAir.seconds < 0) {
        showDetailsEpisodeAirtime.text = getString(R.string.textAiredAlready)
        return
      }
      val days = timeToAir.toDays()
      if (days == 0L) {
        val hours = timeToAir.toHours()
        if (hours == 0L) {
          val minutes = timeToAir.toMinutes()
          showDetailsEpisodeAirtime.text = getQuantityString(R.plurals.textMinutesToAir, minutes)
          return
        }
        showDetailsEpisodeAirtime.text = getQuantityString(R.plurals.textHoursToAir, hours)
        return
      }
      showDetailsEpisodeAirtime.text = getQuantityString(R.plurals.textDaysToAir, days)
    }
  }

  private fun renderImage(image: Image) {
    showDetailsImageProgress.visible()
    Glide.with(this)
      .load("$TVDB_IMAGE_BASE_URL${image.fileUrl}")
      .transform(CenterCrop())
      .transition(withCrossFade(200))
      .withFailListener { showDetailsImageProgress.gone() }
      .withSuccessListener { showDetailsImageProgress.gone() }
      .into(showDetailsImage)
  }

  private fun handleBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      if (showDetailsEpisodesView.isVisible) {
        hideEpisodesView()
        return@addCallback
      }
      remove()
      getMainActivity().showNavigation()
      findNavController().popBackStack()
    }
  }
}
