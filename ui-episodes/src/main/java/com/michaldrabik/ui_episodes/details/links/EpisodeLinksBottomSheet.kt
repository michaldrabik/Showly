package com.michaldrabik.ui_episodes.details.links

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.core.os.bundleOf
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.openWebUrl
import com.michaldrabik.ui_base.utilities.extensions.requireParcelable
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_navigation.java.NavigationArgs
import com.michaldrabik.ui_episodes.R
import com.michaldrabik.ui_episodes.databinding.ViewEpisodeLinksBinding
import com.michaldrabik.ui_model.IdTmdb
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize

@AndroidEntryPoint
class EpisodeLinksBottomSheet : BaseBottomSheetFragment(R.layout.view_episode_links) {

  @Parcelize
  data class Options(
    val showIdTmdb: IdTmdb,
    val ids: Ids,
    val title: String,
    val season: Int,
    val episodeNumber: Int,
  ) : Parcelable

  companion object {
    fun createBundle(showIdTmdb: IdTmdb, episode: Episode): Bundle {
      val options = Options(showIdTmdb, episode.ids, episode.title, episode.season, episode.number)
      return bundleOf(NavigationArgs.ARG_EPISODE_LINK to options)
      }
  }

  private val binding by viewBinding(ViewEpisodeLinksBinding::bind)

  private val options by lazy { requireParcelable<Options>(NavigationArgs.ARG_EPISODE_LINK) }
  private val showIdTmdb by lazy { options.showIdTmdb.id }
  private val ids by lazy { options.ids }
  private val title by lazy { options.title }
  private val season by lazy { options.season }
  private val episodeNumber by lazy { options.episodeNumber }

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
  }

  private fun setupView() {
    val query = "$title season $season episode $episodeNumber"
    with(binding) {
      viewEpisodeLinksGoogle.onClick {
        openWebUrl("https://www.google.com/search?q=$query")
      }
      viewEpisodeLinksDuckDuck.onClick {
        openWebUrl("https://duckduckgo.com/?q=$query")
      }
      viewEpisodeLinksButtonClose.onClick { closeSheet() }
    }
    setTmdbLink()
    setImdbLink()
  }

  private fun setTmdbLink() {
    binding.viewEpisodeLinksTmdb.run {
      if (ids.tmdb.id == -1L) {
        alpha = 0.5F
        isEnabled = false
      } else {
        onClick {
          openWebUrl("https://www.themoviedb.org/tv/${showIdTmdb}/season/${season}/episode/${episodeNumber}")
        }
      }
    }
  }

  private fun setImdbLink() {
    binding.viewEpisodeLinksImdb.run {
      if (ids.imdb.id.isBlank()) {
        alpha = 0.5F
        isEnabled = false
      } else {
        onClick {
          val i = Intent(Intent.ACTION_VIEW)
          i.data = Uri.parse("imdb:///title/${ids.imdb.id}")
          try {
            startActivity(i)
          } catch (e: ActivityNotFoundException) {
            // IMDb App not installed. Start in web browser
            openWebUrl("https://www.imdb.com/title/${ids.imdb.id}")
          }
        }
      }
    }
  }
}
