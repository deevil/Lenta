package com.lenta.shared.features.app_updates

import androidx.lifecycle.MutableLiveData
import app_update.AppUpdateInstaller
import com.lenta.shared.features.loading.startProgressTimer
import com.lenta.shared.platform.navigation.CoreNavigator
import com.lenta.shared.platform.resources.ISharedStringResourceManager
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.launchUITryCatch
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AppUpdateViewModel : CoreViewModel() {
    @Inject
    lateinit var coreNavigator: CoreNavigator
    @Inject
    lateinit var resourceManager: ISharedStringResourceManager

    var appUpdateInstaller: AppUpdateInstaller? = null

    val requiredAppUpdateInstaller: AppUpdateInstaller by lazy {
        appUpdateInstaller!!
    }


    val elapsedTime: MutableLiveData<Long> = MutableLiveData(0)
    val remainingTime: MutableLiveData<Long> = MutableLiveData()
    val progress = MutableLiveData(true)
    val title = MutableLiveData("")

    init {
        launchUITryCatch {
            startProgressTimer(
                    coroutineScope = this,
                    elapsedTime = elapsedTime
            )
            if (appUpdateInstaller == null) {
                coreNavigator.goBack()
            } else {
                loadingNewAppVersion()
            }
        }
    }

    private fun loadingNewAppVersion() {
        launchUITryCatch {
            title.value = resourceManager.checkAppUpdates()
            withContext(IO) {
                requiredAppUpdateInstaller.checkNeedAndHaveUpdate(codeVersion = null)
            }.either({
                coreNavigator.goBack()
                coreNavigator.openAlertScreen(it)
            }) { fileName ->
                installUpdate(fileName = fileName)
            }
        }

    }

    private fun installUpdate(fileName: String) {
        Logg.d { "apk update fileName: $fileName" }
        launchUITryCatch {
            title.value = resourceManager.loadingNewAppVersion()
            withContext(IO) {
                requiredAppUpdateInstaller.installUpdate(fileName = fileName).either({
                    coreNavigator.goBack()
                    coreNavigator.openAlertScreen(it)
                }) {
                    // do nothing. App is finished
                }
            }
        }
    }
}

