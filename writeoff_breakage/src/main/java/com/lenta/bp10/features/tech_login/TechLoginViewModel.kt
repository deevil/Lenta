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
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        screenNavigator.openAlertScreen("onClickApp")
    }

    override fun onBackPressed() {
    }
}