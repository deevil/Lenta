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
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        screenNavigator.openAlertScreen("onClickExit")
    }

    override fun onClickTestEnvir() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        screenNavigator.openAlertScreen("onClickTestEnvir")
    }

    override fun onClickWorkEnvir() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        screenNavigator.openAlertScreen("onClickWorkEnvir")
    }

    override fun onBackPressed() {
    }

}