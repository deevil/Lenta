package com.lenta.inventory.main

import androidx.lifecycle.viewModelScope
import com.lenta.inventory.models.IPersistInventoryTask
import com.lenta.inventory.models.task.IInventoryTaskManager
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.shared.exception.Failure
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
    lateinit var inventoryTaskManager: IInventoryTaskManager
    @Inject
    lateinit var persistInventoryTask: IPersistInventoryTask


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
        persistInventoryTask.saveWriteOffTask(inventoryTaskManager.getInventoryTask())
    }

    override fun preparingForExit() {
        persistInventoryTask.saveWriteOffTask(inventoryTaskManager.getInventoryTask())
    }


}