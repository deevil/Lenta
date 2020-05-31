package com.lenta.movement.features.main

import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class MainMenuViewModel: CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

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

}