package com.lenta.bp10.features.fmp_settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import kotlinx.coroutines.launch
import javax.inject.Inject

class FmpSettingsViewModel : CoreViewModel() {

    @Inject
    lateinit var appSettings: IAppSettings

    @Inject
    lateinit var screenNavigator: IScreenNavigator

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
        screenNavigator.goBack()
        screenNavigator.finishApp()

    }
}
