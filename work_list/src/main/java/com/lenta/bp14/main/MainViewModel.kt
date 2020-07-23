package com.lenta.bp14.main

import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.IGeneralTaskManager
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.sound.ISoundPlayer
import com.lenta.shared.features.loading.startProgressTimer
import com.lenta.shared.platform.activity.main_activity.CoreMainViewModel
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.statusbar.StatusBarUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel : CoreMainViewModel() {

    @Inject
    override lateinit var statusBarUiModel: StatusBarUiModel
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var soundPlayer: ISoundPlayer
    @Inject
    lateinit var generalTaskManager: IGeneralTaskManager


    override fun onNewEnter() {
        screenNavigator.openFirstScreen()
    }

    var progressJob: Job? = null

    override fun showSimpleProgress(title: String, handleFailure: ((Failure) -> Unit)?) {
        progressJob = viewModelScope.launch {
            with(loadingViewModel) {
                this.progress.postValue(true)
                this.title.postValue(title)
                this.elapsedTime.postValue(null)
            }

            startProgressTimer(
                    coroutineScope = this,
                    remainingTime = loadingViewModel.remainingTime,
                    timeoutInSec = Constants.ONE_MINUTE_TIMEOUT,
                    hideProgress = ::hideProgress,
                    handleFailure = handleFailure
            )
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

    override fun onResume() {
        super.onResume()
        soundPlayer.start()
    }

    override fun onPause() {
        super.onPause()
        preparingForExit()
    }

    override fun preparingForExit() {
        soundPlayer.stop()
        generalTaskManager.saveTaskData()
    }

}