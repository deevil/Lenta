package com.lenta.bp10.features.loading.tasks_settings

import androidx.lifecycle.MutableLiveData
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.repos.IRepoInMemoryHolder
import com.lenta.bp10.requests.network.GetUserResourcesNetRequest
import com.lenta.bp10.requests.network.UserResourceInfoParams
import com.lenta.bp10.requests.network.UserResourcesResult
import com.lenta.shared.account.ISessionInfo
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
    lateinit var userResourcesMultiRequest: GetUserResourcesNetRequest

    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    @Inject
    lateinit var sessionInfo: ISessionInfo

    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    init {
        launchUITryCatch {
            progress.value = true
            userResourcesMultiRequest(UserResourceInfoParams(sessionInfo.userName)).either(::handleFailure, ::handleSuccess)
            progress.value = false
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openMainMenuScreen()
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleSuccess(@Suppress("UNUSED_PARAMETER") userResourcesResult: UserResourcesResult) {
        repoInMemoryHolder.userResourceResult = userResourcesResult
        screenNavigator.openMainMenuScreen()
        screenNavigator.openJobCardScreen()
    }

    override fun clean() {
        progress.postValue(false)
    }

}