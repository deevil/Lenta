package com.lenta.bp16.main

import androidx.lifecycle.viewModelScope
import com.lenta.bp16.platform.navigation.IScreenNavigator
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
    lateinit var navigator: IScreenNavigator


    override fun onNewEnter() {
        navigator.openFirstScreen()
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
        bottomToolbarUiModel.visibility.value = false
    }

    override fun hideProgress() {
        loadingViewModel.clean()
        progressJob?.cancel()
        bottomToolbarUiModel.visibility.value = true
    }

    fun onExitClick() {
        navigator.openExitConfirmationScreen()
    }

    override fun preparingForExit() {
        // Реализовать подготовку к выходу из приложения

    }

}