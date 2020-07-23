package com.lenta.movement.main

import androidx.lifecycle.viewModelScope
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.loading.startProgressTimer
import com.lenta.shared.platform.activity.main_activity.CoreMainViewModel
import com.lenta.shared.platform.statusbar.StatusBarUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel: CoreMainViewModel() {

    @Inject
    override lateinit var statusBarUiModel: StatusBarUiModel
    @Inject
    lateinit var screenNavigator: IScreenNavigator

    var progressJob: Job? = null

    override fun onNewEnter() {
        screenNavigator.openFirstScreen()
    }

    override fun showSimpleProgress(title: String, handleFailure: ((Failure) -> Unit)?) {
        progressJob = viewModelScope.launch {
            loadingViewModel.let {
                it.progress.value = true
                it.title.value = title
                it.elapsedTime.value = null
                startProgressTimer(
                    coroutineScope = this,
                    remainingTime = it.remainingTime,
                    timeoutInSec = TIMEOUT_SEC
                )
            }

        }
        bottomToolbarUiModel.hide()
    }

    override fun hideProgress() {
        loadingViewModel.clean()
        progressJob?.cancel()
        bottomToolbarUiModel.show()
    }

    fun onExitClick() {
        screenNavigator.openExitConfirmationScreen()
    }

    override fun preparingForExit() = Unit

    companion object {
        private const val TIMEOUT_SEC = 60
    }
}