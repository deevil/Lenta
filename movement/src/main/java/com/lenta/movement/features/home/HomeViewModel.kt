package com.lenta.movement.features.home

import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class HomeViewModel: CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    fun onClickAuxiliaryMenu() {
        screenNavigator.openAuxiliaryMenuScreen()
    }

}