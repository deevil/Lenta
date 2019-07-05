package com.lenta.bp7.features.loading.fast

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IRepoInMemoryHolder
import com.lenta.bp7.requests.network.RequestParams
import com.lenta.bp7.requests.network.SettingRequest
import com.lenta.bp7.requests.network.SettingRequestResult
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.platform.app_update.AppUpdateChecker
import kotlinx.coroutines.launch
import javax.inject.Inject

class FastLoadingViewModel : CoreLoadingViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var failureInterpreter: IFailureInterpreter
    @Inject
    lateinit var appUpdateChecker: AppUpdateChecker
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder
    @Inject
    lateinit var settingRequest: SettingRequest


    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    init {
        viewModelScope.launch {
            progress.value = true
            settingRequest(RequestParams(login = "")).either(::handleFailure, ::handleSuccess)
            progress.value = false
        }
    }

    override fun handleFailure(failure: Failure) {
        navigator.openSelectMarketScreen()
        navigator.openAlertScreen(failureInterpreter.getFailureDescription(failure).message)
    }

    private fun handleSuccess(settingRequestResult: SettingRequestResult) {
        repoInMemoryHolder.settingRequestResult = settingRequestResult

        navigator.openOptionScreen()
    }

    override fun clean() {
        progress.postValue(false)
    }
}