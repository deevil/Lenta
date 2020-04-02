package com.lenta.bp12.features.main_menu

import com.lenta.bp12.model.Mode
import com.lenta.bp12.model.ITaskManager
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class MainMenuViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var manager: ITaskManager


    val employeeName: String by lazy {
        sessionInfo.personnelFullName ?: ""
    }

    // -----------------------------

    fun onClickAuxiliaryMenu() {
        navigator.openAuxiliaryMenuScreen()
    }

    fun onClickUser() {
        navigator.openEnterEmployeeNumberScreen()
    }

    fun createTask() {
        manager.mode = Mode.CREATE_TASK
        navigator.openTaskCardCreateScreen()
    }

    fun workWithTask() {
        manager.mode = Mode.WORK_WITH_TASK
        navigator.openTaskListScreen()
    }

}