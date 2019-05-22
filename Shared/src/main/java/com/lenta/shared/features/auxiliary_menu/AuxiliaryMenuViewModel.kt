package com.lenta.shared.features.auxiliary_menu

import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class AuxiliaryMenuViewModel : CoreViewModel() {
    @Inject
    lateinit var screenNavigator: ICoreNavigator

    fun onClickHome() {
        screenNavigator.goBack()
    }

    fun onClickSettings() {
        screenNavigator.openSettingsScreen()
    }

    fun onClickSupport() {
        screenNavigator.openSupportScreen()
    }

    fun onBackPressed() {
    }
}