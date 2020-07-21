package com.lenta.movement.features.main

import com.lenta.movement.platform.extensions.unsafeLazy
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class MainMenuViewModel: CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    val fio by unsafeLazy {
        sessionInfo.personnelFullName
    }

    fun onClickAuxiliaryMenu() {
        screenNavigator.openAuxiliaryMenuScreen()
    }

    fun onClickCreateBox() {
        screenNavigator.openGoodsList()
    }

    fun onClickCreateTask() {
        screenNavigator.openTaskScreen(null)
    }

    fun onClickWorkWithTask() {
        screenNavigator.openTaskList()
    }

    fun onClickUser(){
        screenNavigator.closeAllScreen()
        screenNavigator.openSelectionPersonnelNumberScreen()
    }

}