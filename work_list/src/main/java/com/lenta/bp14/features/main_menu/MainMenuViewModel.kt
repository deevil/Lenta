package com.lenta.bp14.features.main_menu

import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class MainMenuViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo


    private val authorizationSkipped by lazy {
        sessionInfo.isAuthSkipped.map { it == true }
    }

    val authorizationButtonVisibility by lazy { authorizationSkipped.map { it } }


    val createTaskButtonVisibility by lazy { authorizationSkipped.map { it == false } }
    val workWithTaskButtonVisibility by lazy { authorizationSkipped.map { it == false } }
    val checkListButtonVisibility by lazy { authorizationSkipped.map { it == true } }


    fun onClickPrint() {
        // Тестирование запуска различных экранов
        //navigator.openPrintSettingsScreen()
        //navigator.openGoodInfoWlScreen()
        //navigator.openGoodsListWlScreen()
        //navigator.openGoodsListPcScreen()
        //navigator.openSearchFilterWlScreen()
        //navigator.openGoodsListNeScreen()
        //navigator.openGoodDetailsScreen()
        //navigator.openTestScanBarcodeScreen()
        //navigator.openSearchFilterTlScreen()
        //navigator.openTaskListScreen()
        //navigator.openListOfDifferencesScreen()
        //navigator.openExpectedDeliveriesScreen()
        //navigator.openGoodSalesScreen()
        //navigator.openGoodInfoNeScreen()
        //navigator.openGoodInfoPcScreen()
        navigator.openGoodsListClScreen()
    }

    fun onClickCreateTask() {
        navigator.openJobCardScreen()

    }

    fun onClickWorkWithTask() {
        navigator.openTaskListScreen()
    }

    fun onClickCheckList() {
        navigator.openGoodsListClScreen()
    }

    fun onClickAuxiliaryMenu() {
        navigator.openAuxiliaryMenuScreen()
    }

    fun onClickAuthorization() {
        navigator.closeAllScreen()
        navigator.openLoginScreen()
    }

}