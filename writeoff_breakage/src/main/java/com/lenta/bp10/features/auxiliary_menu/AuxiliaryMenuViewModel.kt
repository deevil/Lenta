package com.lenta.bp10.features.auxiliary_menu

import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.features.auxiliary_menu.CoreAuxiliaryMenuViewModel
import javax.inject.Inject

class AuxiliaryMenuViewModel : CoreAuxiliaryMenuViewModel(){

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    override fun onClickHome() {
        screenNavigator.goBack()
    }

    override fun onClickExit() {
        //TODO реализовать вызов диалога по выходу их приложения
        screenNavigator.openAlertScreen("onClickExit")
    }

    override fun onClickSettings() {
        screenNavigator.openSettingsScreen()
    }

    override fun onClickSupport() {
        screenNavigator.openSupportScreen()
    }

    override fun onBackPressed() {
    }
}