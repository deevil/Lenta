package com.lenta.bp10.features.settings

import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.features.settings.CoreSettingsViewModel
import javax.inject.Inject

class SettingsViewModel : CoreSettingsViewModel() {
    @Inject
    lateinit var screenNavigator: IScreenNavigator

    override fun onClickBack() {
        screenNavigator.goBack()
    }

    override fun onClickExit() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        screenNavigator.openAlertScreen("onClickExit")
    }

    override fun onClickPrinter() {
        screenNavigator.openPrinterChangeScreen()
    }

    override fun onClickWork() {
        screenNavigator.openSelectOperModeScreen()
    }

    override fun onClickTechLog() {
        screenNavigator.openTechLoginScreen()
    }

    override fun onBackPressed() {
    }

}