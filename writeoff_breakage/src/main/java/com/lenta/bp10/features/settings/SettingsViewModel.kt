package com.lenta.bp10.features.settings

import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class SettingsViewModel : CoreViewModel() {
    @Inject
    lateinit var screenNavigator: IScreenNavigator

    fun onClick1() {
        screenNavigator.openAlertScreen("onClick1")

    }

}