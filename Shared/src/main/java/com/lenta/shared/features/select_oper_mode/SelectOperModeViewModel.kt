package com.lenta.shared.features.select_oper_mode

import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class SelectOperModeViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: ICoreNavigator



    fun onClickTestEnvir() {
        screenNavigator.openTestEnvirScreen()
    }

    fun onClickWorkEnvir() {
        //TODO смена среды на рабочую
        screenNavigator.goBack()
    }

    fun onClickSettingsConnections() {
        screenNavigator.openConnectionsSettingsScreen()
    }

    fun oClickBack() {
        screenNavigator.goBack()
    }

}