package com.lenta.bp9.features.loading.fast

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app_update.AppUpdateInstaller
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.network.FastResourcesMultiRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.functional.Either
import com.lenta.shared.platform.resources.ISharedStringResourceManager
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.requests.network.ServerTime
import com.lenta.shared.requests.network.ServerTimeRequest
import com.lenta.shared.requests.network.ServerTimeRequestParam
import com.lenta.shared.utilities.Logg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    @Inject
    lateinit var appUpdateInstaller: AppUpdateInstaller
    @Inject
    lateinit var resourceManager: ISharedStringResourceManager
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    init {
        viewModelScope.launch {
            progress.value = true
            withContext(Dispatchers.IO) {
                repoInMemoryHolder.permissions?.markets?.find { it.number == sessionInfo.market }.let { market ->
                    val codeVersion = market?.version?.toIntOrNull()
                    Logg.d { "codeVersion for update: $codeVersion" }
                    if (codeVersion == null) {
                        Either.Right("")
                    } else {
                        appUpdateInstaller.checkNeedAndHaveUpdate(codeVersion)
                    }
                }
            }.either({
                Logg.e { "checkNeedAndHaveUpdate failure: $it" }
                handleFailure(failure = it)
            }) { updateFileName ->
                Logg.d { "update fileName: $updateFileName" }
                if (updateFileName.isBlank()) {
                    getServerTime()
                } else {
                    installUpdate(updateFileName)
                }
            }
        }
    }

    private fun installUpdate(updateFileName: String) {
        viewModelScope.launch {
            title.value = resourceManager.loadingNewAppVersion()
            progress.value = true
            withContext(Dispatchers.IO) {
                appUpdateInstaller.installUpdate(updateFileName)
            }.either(::handleFailure) {
                // do nothing. App is finished
            }
        }

    }

    private fun getServerTime() {
        viewModelScope.launch {
            serverTimeRequest(ServerTimeRequestParam(sessionInfo.market
                    ?: "")).either(::handleFailure, ::handleSuccessServerTime)
        }
    }

    private fun handleSuccessServerTime(serverTime: ServerTime) {
        timeMonitor.setServerTime(time = serverTime.time, date = serverTime.date)
        viewModelScope.launch {
            fastResourcesNetRequest(null).either(::handleFailure, ::handleSuccess)
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openLoginScreen()
        screenNavigator.openAlertScreen(failureInterpreter.getFailureDescription(failure).message)
        progress.postValue(false)
    }

    private fun handleSuccess(@Suppress("UNUSED_PARAMETER") b: Boolean) {
        screenNavigator.openSelectionPersonnelNumberScreen(isScreenMainMenu = false)
        progress.value = false
    }

    override fun clean() {
        progress.postValue(false)
    }
}
