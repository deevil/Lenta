package com.lenta.shared.features.settings

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class SettingsViewModel : CoreViewModel(){

    @Inject
    lateinit var hyperHive: HyperHive

    var isMainMenu: MutableLiveData<Boolean> = MutableLiveData(true)

    @Inject
    lateinit var screenNavigator: ICoreNavigator

    fun onClickBack() {
        screenNavigator.goBack()
    }

    fun onClickPrinter() {
        screenNavigator.openPrinterChangeScreen()
    }

    fun onClickWork() {
        screenNavigator.openSelectOperModeScreen()
    }

    fun onClickTechLog() {
        screenNavigator.openTechLoginScreen()
    }


}