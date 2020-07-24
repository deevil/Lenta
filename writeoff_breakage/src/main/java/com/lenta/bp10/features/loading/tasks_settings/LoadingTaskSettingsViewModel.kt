package com.lenta.bp10.features.loading.tasks_settings

import androidx.lifecycle.MutableLiveData
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.requests.network.loader.UserResourcesMultiRequest
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.utilities.extentions.launchUITryCatch
import javax.inject.Inject

class LoadingTaskSettingsViewModel : CoreLoadingViewModel() {
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var failureInterpreter: IFailureInterpreter
    @Inject
    lateinit var userResourcesMultiRequest: UserResourcesMultiRequest

    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    init {
        launchUITryCatch {
            progress.value = true
            userResourcesMultiRequest(null).either(::handleFailure, ::handleSuccess)
            progress.value = false
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openMainMenuScreen()
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleSuccess(@Suppress("UNUSED_PARAMETER") b: Boolean) {
        screenNavigator.openMainMenuScreen()
        screenNavigator.openJobCardScreen()
    }

    override fun clean() {
        progress.postValue(false)
    }

}