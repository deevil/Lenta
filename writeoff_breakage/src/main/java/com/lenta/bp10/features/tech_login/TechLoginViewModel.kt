package com.lenta.bp10.features.tech_login

import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.features.tech_login.CoreTechLoginViewModel
import javax.inject.Inject

class TechLoginViewModel : CoreTechLoginViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    override fun onClickBack() {
        screenNavigator.goBack()
    }

    override fun onClickApp() {
        //TODO смены тех.пользователя
        screenNavigator.openLoginScreen()
    }

    override fun onBackPressed() {
    }
}