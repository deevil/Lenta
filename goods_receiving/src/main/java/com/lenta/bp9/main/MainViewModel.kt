package com.lenta.bp9.main

import androidx.lifecycle.viewModelScope
import com.lenta.bp9.platform.navigation.IScreenNavigator
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
        bottomToolbarUiModel.visibility.value = false
    }

    override fun hideProgress() {
        loadingViewModel.clean()
        progressJob?.cancel()
        bottomToolbarUiModel.visibility.value = true
    }

    fun onExitClick() {
        screenNavigator.openExitConfirmationScreen()
    }


    override fun onPause() {
        super.onPause()
        //TODO реализовать сохранение задачи
        //persistWriteOffTask.saveWriteOffTask(writeOffTaskManager.getWriteOffTask())
    }



}