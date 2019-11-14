package com.lenta.bp16.features.main_menu

import com.lenta.bp16.platform.navigation.IScreenNavigator
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


    fun onClickProcessing() {

    }

    fun onClickAuxiliaryMenu() {
        navigator.openAuxiliaryMenuScreen()
    }

    fun onClickAuthorization() {
        navigator.closeAllScreen()
        navigator.openLoginScreen()
    }

}