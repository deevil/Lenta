package com.lenta.shared.features.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.PackageName
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsViewModel : CoreViewModel(){

    @Inject
    lateinit var hyperHive: HyperHive
    @Inject
    lateinit var screenNavigator: ICoreNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo


    var isMainMenu: MutableLiveData<Boolean> = MutableLiveData(true)

    var selectPrinterButtonVisibility: MutableLiveData<Boolean> = MutableLiveData(true)
    var selectOperModeButtonVisibility: MutableLiveData<Boolean> = MutableLiveData(true)

    init {
        viewModelScope.launch {
            when (sessionInfo.packageName) {
                PackageName.PLE.path -> {
                    selectPrinterButtonVisibility.value = false
                    selectOperModeButtonVisibility.value = false
                }
            }
        }
    }

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