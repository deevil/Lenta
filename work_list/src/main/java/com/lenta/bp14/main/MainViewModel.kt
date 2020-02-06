package com.lenta.bp14.main

import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.IGeneralTaskManager
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.platform.sound.ISoundPlayer
import com.lenta.shared.features.loading.startProgressTimer
import com.lenta.shared.platform.activity.main_activity.CoreMainViewModel
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

    override fun showSimpleProgress(title: String) {
        progressJob = viewModelScope.launch {
            loadingViewModel.let {
                it.progress.value = true
                it.title.value = title
                it.elapsedTime.value = null
                startProgressTimer(
                        coroutineScope = this,
                        remainingTime = it.remainingTime,
                        timeoutInSec = 60
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