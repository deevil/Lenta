package com.lenta.bp9.features.loading.fast

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.FastResourcesMultiRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.requests.network.ServerTime
import com.lenta.shared.requests.network.ServerTimeRequest
import com.lenta.shared.requests.network.ServerTimeRequestParam
import kotlinx.coroutines.launch
import javax.inject.Inject

class FastDataLoadingViewModel : CoreLoadingViewModel() {

    @Inject
    lateinit var fastResourcesNetRequest: FastResourcesMultiRequest
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var serverTimeRequest: ServerTimeRequest
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var timeMonitor: ITimeMonitor
    @Inject
    lateinit var failureInterpreter: IFailureInterpreter

    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    init {
        viewModelScope.launch {
            progress.value = true
            serverTimeRequest(ServerTimeRequestParam(sessionInfo.market
                    ?: "")).either(::handleFailure, ::handleSuccessServerTime)
            progress.value = false
        }
    }

    private fun handleSuccessServerTime(serverTime: ServerTime) {
        timeMonitor.setServerTime(time = serverTime.time, date = serverTime.date)
        viewModelScope.launch {
            progress.value = true
            fastResourcesNetRequest(null).either(::handleFailure, ::handleSuccess)
            progress.value = false
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openLoginScreen()
        screenNavigator.openAlertScreen(failureInterpreter.getFailureDescription(failure).message)

    }

    private fun handleSuccess(@Suppress("UNUSED_PARAMETER") b: Boolean) {
        screenNavigator.openSelectionPersonnelNumberScreen()
    }

    override fun clean() {
        progress.postValue(false)
    }
}
