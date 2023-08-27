package com.michaldrabik.ui_trakt_sync

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaldrabik.common.extensions.dateFromMillis
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.data_remote.Config
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnTraktAuthorizeListener
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.openWebUrl
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_model.TraktSyncSchedule
import com.michaldrabik.ui_trakt_sync.TraktSyncUiEvent.Finish
import com.michaldrabik.ui_trakt_sync.TraktSyncUiEvent.RequestNotificationsPermission
import com.michaldrabik.ui_trakt_sync.TraktSyncUiEvent.StartAuthorization
import com.michaldrabik.ui_trakt_sync.databinding.FragmentTraktSyncBinding
import com.michaldrabik.ui_trakt_sync.views.NotificationsRationaleView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TraktSyncFragment :
  BaseFragment<TraktSyncViewModel>(R.layout.fragment_trakt_sync),
  OnTraktAuthorizeListener {

  override val viewModel by viewModels<TraktSyncViewModel>()
  private val binding by viewBinding(FragmentTraktSyncBinding::bind)

  override fun onResume() {
    super.onResume()
    hideNavigation()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupStatusBar()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      { viewModel.eventFlow.collect { handleEvent(it) } },
      { viewModel.messageFlow.collect { showSnack(it) } },
      doAfterLaunch = { viewModel.invalidate() }
    )
  }

  private fun setupView() {
    with(binding) {
      traktSyncToolbar.setNavigationOnClickListener { activity?.onBackPressed() }
      traktSyncImportCheckbox.setOnCheckedChangeListener { _, isChecked ->
        traktSyncButton.isEnabled = (isChecked || traktSyncExportCheckbox.isChecked)
      }
      traktSyncExportCheckbox.setOnCheckedChangeListener { _, isChecked ->
        traktSyncButton.isEnabled = (isChecked || traktSyncImportCheckbox.isChecked)
      }
    }
  }

  private fun setupStatusBar() {
    binding.traktSyncRoot.doOnApplyWindowInsets { view, insets, _, _ ->
      val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
      view.updatePadding(top = inset)
    }
  }

  private fun checkScheduleImport(currentSchedule: TraktSyncSchedule?, quickSyncEnabled: Boolean?) {
    if (quickSyncEnabled == true && currentSchedule == TraktSyncSchedule.OFF) {
      MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
        .setTitle(R.string.textSettingsScheduleImportConfirmationTitle)
        .setMessage(R.string.textSettingsScheduleImportConfirmationMessage)
        .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
        .setPositiveButton(R.string.textYes) { _, _ -> scheduleImport(currentSchedule) }
        .setNegativeButton(R.string.textCancel) { _, _ -> }
        .show()
    } else {
      scheduleImport(currentSchedule)
    }
  }

  private fun scheduleImport(currentSchedule: TraktSyncSchedule?) {
    val options = TraktSyncSchedule.values()
    val optionsStrings = options.map { getString(it.stringRes) }.toTypedArray()
    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setSingleChoiceItems(optionsStrings, options.indexOf(currentSchedule)) { dialog, index ->
        val schedule = options[index]
        viewModel.saveTraktSyncSchedule(schedule)
        showSnack(MessageEvent.Info(schedule.confirmationStringRes))
        dialog.dismiss()
      }
      .show()
  }

  override fun onAuthorizationResult(authData: Uri?) {
    val code = authData?.getQueryParameter("code")
    viewModel.authorizeTrakt(code)
  }

  @SuppressLint("InlinedApi")
  private fun showNotificationsRationaleDialog() {
    val context = requireContext()
    val view = NotificationsRationaleView(context)
    MaterialAlertDialogBuilder(context, R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(context, R.drawable.bg_dialog))
      .setView(view)
      .setPositiveButton(R.string.textYes) { _, _ ->
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
      .setNegativeButton(R.string.textNo) { _, _ -> openTraktAuthWebsite() }
      .show()
  }

  private fun openTraktAuthWebsite() {
    openWebUrl(Config.TRAKT_AUTHORIZE_URL) ?: showSnack(MessageEvent.Error(R.string.errorCouldNotFindApp))
  }

  private fun render(uiState: TraktSyncUiState) {
    uiState.run {
      with(binding) {
        isProgress.let {
          traktSyncButton.visibleIf(!it, false)
          traktSyncProgress.visibleIf(it)
          traktSyncImportCheckbox.visibleIf(!it)
          traktSyncExportCheckbox.visibleIf(!it)
          traktSyncScheduleButton.visibleIf(!it)
          traktLastSyncTimestamp.visibleIf(!it)
        }
        traktSyncStatus.text = progressStatus
        traktSyncScheduleButton.setText(traktSyncSchedule.buttonStringRes)

        if (lastTraktSyncTimestamp != 0L) {
          val date = dateFormat?.format(dateFromMillis(lastTraktSyncTimestamp).toLocalZone())?.capitalizeWords()
          traktLastSyncTimestamp.text = getString(R.string.textTraktSyncLastTimestamp, date)
        }

        if (isAuthorized) {
          traktSyncButton.text = getString(R.string.textTraktSyncStart)
          traktSyncButton.onClick {
            viewModel.startImport(
              isImport = traktSyncImportCheckbox.isChecked,
              isExport = traktSyncExportCheckbox.isChecked
            )
          }
          traktSyncScheduleButton.onClick { checkScheduleImport(traktSyncSchedule, quickSyncEnabled) }
        } else {
          traktSyncButton.text = getString(R.string.textSettingsTraktAuthorizeTitle)
          traktSyncButton.onClick {
            viewModel.checkNotificationsPermission(requireAppContext())
          }
          traktSyncScheduleButton.gone()
        }
      }
    }
  }

  private fun handleEvent(event: Event<*>) {
    when (event) {
      StartAuthorization -> openTraktAuthWebsite()
      RequestNotificationsPermission -> showNotificationsRationaleDialog()
      Finish -> activity?.onBackPressed()
    }
  }

  private val requestPermissionLauncher =
    registerForActivityResult(RequestPermission()) { _ ->
      openTraktAuthWebsite()
    }
}
