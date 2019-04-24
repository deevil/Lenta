package com.lenta.bp10.features.settings

import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.features.settings.CoreSettingsViewModel
import javax.inject.Inject

class SettingsViewModel : CoreSettingsViewModel() {
    @Inject
    lateinit var screenNavigator: IScreenNavigator

    override fun onClickHome() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        screenNavigator.openAlertScreen("onClickHome")
    }

    override fun onClickExit() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        screenNavigator.openAlertScreen("onClickExit")
    }

    override fun onClickPrinter() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        screenNavigator.openAlertScreen("onClickPrinter")
    }

    override fun onClickWork() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        screenNavigator.openAlertScreen("onClickWork")
    }

    override fun onClickTechLog() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        screenNavigator.openAlertScreen("onClickTechLog")
    }

    override fun onBackPressed() {
    }

}