package com.lenta.bp10.activity.main

import com.lenta.shared.requests.network.Auth
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.activity.main_activity.CoreMainViewModel
import com.lenta.shared.platform.statusbar.StatusBarUiModel
import javax.inject.Inject

class MainViewModel : CoreMainViewModel() {
    @Inject
    override lateinit var statusBarUiModel: StatusBarUiModel
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var auth: Auth

    override fun onNewEnter() {
        screenNavigator.openFirstScreen()
    }


}