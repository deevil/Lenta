package com.lenta.bp10.features.select_oper_mode

import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.features.select_oper_mode.CoreSelectOperModeViewModel
import javax.inject.Inject

class SelectOperModeViewModel : CoreSelectOperModeViewModel(){


    @Inject
    lateinit var screenNavigator: IScreenNavigator

    override fun onClickBack() {
        screenNavigator.goBack()
    }

    override fun onClickExit() {
        screenNavigator.openAlertScreen("onClickExit")
    }

    override fun onClickTestEnvir() {
        screenNavigator.openTestEnvirScreen()
    }

    override fun onClickWorkEnvir() {
        screenNavigator.openAlertScreen("onClickWorkEnvir")
    }

    override fun onClickSettingsConnections() {
        screenNavigator.openConnectionsSettingsScreen()
    }



}