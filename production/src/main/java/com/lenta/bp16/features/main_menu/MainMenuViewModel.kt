package com.lenta.bp16.features.main_menu

import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.model.TaskType
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class MainMenuViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var taskManager: ITaskManager


    fun onClickAuxiliaryMenu() {
        navigator.openAuxiliaryMenuScreen()
    }

    fun onClickExternalSupply() {
        taskManager.taskType = TaskType.EXTERNAL_SUPPLY
        openTaskList()
    }

    fun onClickProcessingUnit() {
        taskManager.taskType = TaskType.PROCESSING_UNIT
        openTaskList()
    }

    private fun openTaskList() {
        navigator.openTaskListScreen()
    }

}