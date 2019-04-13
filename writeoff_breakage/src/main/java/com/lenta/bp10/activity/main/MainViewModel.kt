package com.lenta.bp10.activity.main

import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.activity.main_activity.BaseMainViewModel
import javax.inject.Inject

class MainViewModel: BaseMainViewModel() {
    @Inject
    lateinit var screenNavigator: IScreenNavigator

    fun onNewEnter() {
        screenNavigator.openFirsctScreen()
    }


}