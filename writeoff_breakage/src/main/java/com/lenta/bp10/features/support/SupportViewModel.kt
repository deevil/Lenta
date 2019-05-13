package com.lenta.bp10.features.support

import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.features.support.CoreSupportViewModel
import javax.inject.Inject

class SupportViewModel : CoreSupportViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    override fun onClickBack() {
        screenNavigator.goBack()
    }

    override fun onBackPressed() {
    }
}