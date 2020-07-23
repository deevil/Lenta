package com.lenta.bp7.activity.main

import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.model.CheckData
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.loading.startProgressTimer
import com.lenta.shared.platform.activity.main_activity.CoreMainViewModel
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.statusbar.StatusBarUiModel
import com.lenta.shared.settings.IAppSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel : CoreMainViewModel() {

    @Inject
    override lateinit var statusBarUiModel: StatusBarUiModel
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var checkData: CheckData
    @Inject
    lateinit var appSettings: IAppSettings


    init {
        viewModelScope.launch {
            appSettings.printerNotVisible = true
            appSettings.printerNumber = appSettings.printerNumber
        }
    }


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

    override fun onPause() {
        super.onPause()
        checkData.saveCheckResult()
    }

    override fun preparingForExit() {
        checkData.saveCheckResult()
    }

}