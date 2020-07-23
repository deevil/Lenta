package com.lenta.bp10.activity.main

import androidx.lifecycle.viewModelScope
import com.lenta.bp10.models.IPersistWriteOffTask
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.exception.Failure
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
    lateinit var writeOffTaskManager: IWriteOffTaskManager
    @Inject
    lateinit var persistWriteOffTask: IPersistWriteOffTask


    override fun onNewEnter() {
        screenNavigator.openFirstScreen()
    }

    var progressJob: Job? = null

    override fun showSimpleProgress(title: String, handleFailure: ((Failure) -> Unit)?) {
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

    override fun onPause() {
        super.onPause()
        persistWriteOffTask.saveWriteOffTask(writeOffTaskManager.getWriteOffTask())
    }


    override fun preparingForExit() {
        persistWriteOffTask.saveWriteOffTask(writeOffTaskManager.getWriteOffTask())
    }

}