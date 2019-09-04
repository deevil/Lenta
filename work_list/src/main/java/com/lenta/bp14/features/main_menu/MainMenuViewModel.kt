package com.lenta.bp14.features.main_menu

import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class MainMenuViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator


    fun onClickWorkWithTask() {
        navigator.openTaskListScreen()
    }

    fun onClickCreateTask() {

    }

    fun onClickPrint() {
        // Тестирование запуска различных экранов
        //navigator.openPrintSettingsScreen()
        //navigator.openGoodInfoWlScreen()
        //navigator.openGoodsListWlScreen()
        //navigator.openGoodsListPcScreen()
        //navigator.openSearchFilterWlScreen()
        //navigator.openGoodsListNeScreen()
        //navigator.openGoodsListClScreen()
        //navigator.openGoodDetailsScreen()
        navigator.openSearchFilterTlScreen()
        //navigator.openTaskListScreen()
        //navigator.openListOfDifferencesScreen()
        //navigator.openExpectedDeliveriesScreen()
        //navigator.openGoodSalesScreen()
    }

    fun onClickAuxiliaryMenu() {
        navigator.openAuxiliaryMenuScreen()
    }

}