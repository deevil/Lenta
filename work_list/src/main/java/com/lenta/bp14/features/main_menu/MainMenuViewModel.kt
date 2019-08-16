package com.lenta.bp14.features.main_menu

import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class MainMenuViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator


    fun onClickWorkWithTask() {
        screenNavigator.openTaskListScreen()
    }

    fun onClickCreateTask() {
    }

    fun onClickPrint() {
        //screenNavigator.openPrintSettingsScreen()
        screenNavigator.openGoodInfoWorkListScreen()
    }


    fun onClickAuxiliaryMenu() {
        screenNavigator.openAuxiliaryMenuScreen()
    }
}