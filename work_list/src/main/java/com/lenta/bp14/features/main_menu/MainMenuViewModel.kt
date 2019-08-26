package com.lenta.bp14.features.main_menu

import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class MainMenuViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator


    fun onClickWorkWithTask() {
        screenNavigator.openTaskListScreen()
    }

    fun onClickCreateTask() {
    }

    fun onClickPrint() {
        // Тестирование запуска различных экранов
        //screenNavigator.openPrintSettingsScreen()
        //screenNavigator.openGoodInfoWlScreen()
        screenNavigator.openGoodsListWlScreen()
        //screenNavigator.openGoodListPcScreen()
        //screenNavigator.openSearchFilterWlScreen()
    }


    fun onClickAuxiliaryMenu() {
        screenNavigator.openAuxiliaryMenuScreen()
    }
}