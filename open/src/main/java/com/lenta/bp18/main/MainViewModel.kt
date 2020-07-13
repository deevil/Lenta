package com.lenta.bp18.main

import androidx.lifecycle.viewModelScope
import com.lenta.bp18.platform.Constants
import com.lenta.bp18.platform.navigation.IScreenNavigator
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
        navigator.openAuthScreen()
    }

    var progressJob: Job? = null

    override fun showSimpleProgress(title: String) {
        progressJob = viewModelScope.launch {
            loadingViewModel.let {
                it.progress.postValue(true)
                it.title.postValue(title)
                it.elapsedTime.postValue(null)
                startProgressTimer(
                        coroutineScope = this,
                        remainingTime = it.remainingTime,
                        timeoutInSec = Constants.TIME_OUT_IN_SEC
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
        navigator.openExitConfirmationScreen()
    }

    override fun preparingForExit() {
        // Реализовать подготовку к выходу из приложения

    }

}