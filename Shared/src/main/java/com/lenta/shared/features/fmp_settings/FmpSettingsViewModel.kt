package com.lenta.shared.features.fmp_settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import kotlinx.coroutines.launch
import javax.inject.Inject

class FmpSettingsViewModel : CoreViewModel() {

    @Inject
    lateinit var appSettings: IAppSettings

    @Inject
    lateinit var coreNavigator: ICoreNavigator

    val serverAddress = MutableLiveData<String>()
    val environment = MutableLiveData<String>()
    val project = MutableLiveData<String>()

    init {

        viewModelScope.launch {
            serverAddress.value = appSettings.serverAddress
            environment.value = appSettings.environment
            project.value = appSettings.project

        }

    }


    fun onClickApply() {
        appSettings.serverAddress = serverAddress.value?: ""
        appSettings.environment = environment.value?: ""
        appSettings.project = project.value?: ""
        coreNavigator.finishApp()

    }
}
