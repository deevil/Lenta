package com.lenta.bp10.features.main_menu

import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class MainMenuViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo

    fun onClickCreateTask() {
        screenNavigator.openLoadingTaskSettingsScreen()
    }

    fun onClickAuxiliaryMenu() {
        screenNavigator.openAuxiliaryMenuScreen()
    }
}