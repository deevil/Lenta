package com.lenta.bp12.features.main_menu

import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class MainMenuViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo


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
        navigator.openTaskCardCreateScreen()
    }

    fun workWithTask() {
        navigator.openTaskListScreen()
    }

}