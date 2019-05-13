package com.lenta.bp10.features.main_menu

import androidx.lifecycle.MutableLiveData
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class MainMenuViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    val fio = MutableLiveData("")

    fun onClickCreateTask() {
        screenNavigator.openLoadingTaskSettingsScreen()

    }

    fun onClickUser() {

    }

    fun onClickExit() {
        //TODO To change body
        screenNavigator.openAlertScreen("onClickExit")
    }

    fun onClickAuxiliaryMenu() {
        screenNavigator.openAuxiliaryMenuScreen()
    }
}