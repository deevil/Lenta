package com.lenta.shared.features.fmp_settings

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.extentions.launchUITryCatch
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

        launchUITryCatch {
            serverAddress.value = appSettings.getCurrentServerAddress()
            environment.value = appSettings.getCurrentEnvironment()
            project.value = appSettings.getCurrentProject()
        }

    }


    fun onClickApply() {
        if (appSettings.isTest) {
            appSettings.testServerAddress = serverAddress.value.orEmpty()
            appSettings.testEnvironment = environment.value.orEmpty()
            appSettings.testProject = project.value.orEmpty()

        } else {
            appSettings.serverAddress = serverAddress.value.orEmpty()
            appSettings.environment = environment.value.orEmpty()
            appSettings.project = project.value.orEmpty()
        }

        appSettings.isTest = true
        coreNavigator.finishApp(restart = true)

    }
}
