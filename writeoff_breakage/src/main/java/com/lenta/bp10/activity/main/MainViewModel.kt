package com.lenta.bp10.activity.main

import com.lenta.shared.features.login.usecase.Auth
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.activity.main_activity.CoreMainViewModel
import javax.inject.Inject
import com.lenta.shared.utilities.Logg
class MainViewModel : CoreMainViewModel() {
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var auth: Auth

    fun onNewEnter() {
        screenNavigator.openFirstScreen()
    }

    override fun onCleared() {
        super.onCleared()
        Logg.d { "onCleared" }
    }


}