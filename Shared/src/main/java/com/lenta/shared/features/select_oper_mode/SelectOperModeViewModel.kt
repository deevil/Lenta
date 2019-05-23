package com.lenta.shared.features.select_oper_mode

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.shared.features.test_environment.PinCodeViewModel
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import kotlinx.coroutines.launch
import javax.inject.Inject

class SelectOperModeViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: ICoreNavigator
    @Inject
    lateinit var appSettings: IAppSettings

    val buttonNetworkSettingsEnabled = MutableLiveData(false)
    val buttonTestEnvEnabled = MutableLiveData(false)
    val buttonWorkEnvEnabled = MutableLiveData(false)

    init {
        viewModelScope.launch {
            appSettings.isTest.let {
                buttonTestEnvEnabled.value = !appSettings.isTest
                buttonWorkEnvEnabled.value = appSettings.isTest
                buttonNetworkSettingsEnabled.value = appSettings.isTest
            }

        }

    }


    fun onClickTestEnvir() {
        screenNavigator.openPinCodeForTestEnvironment()
    }

    fun onClickWorkEnvir() {
        appSettings.isTest = false
        screenNavigator.finishApp()
    }

    fun onClickSettingsConnections() {
        screenNavigator.openPinCodeForNetworkSettings()
    }

    fun onPinCodeSuccess(bundle: Bundle) {
        bundle.getInt(PinCodeViewModel.KEY_ARGS_ID_CODE_CONFIRM).let {
            when (it) {
                REQUEST_CODE_NETWORK_SETTINGS -> screenNavigator.openConnectionsSettingsScreen()
                REQUEST_CODE_TEST_ENVIRONMENT -> {
                    appSettings.isTest = true
                    screenNavigator.finishApp()
                }
            }
        }
    }

    fun oClickBack() {
        screenNavigator.goBack()
    }

    companion object {
        const val REQUEST_CODE_NETWORK_SETTINGS = 10
        const val REQUEST_CODE_TEST_ENVIRONMENT = 11
    }

}