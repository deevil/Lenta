package com.lenta.bp18.features.loading.fast

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app_update.AppUpdateInstaller
import com.lenta.bp18.platform.navigation.IScreenNavigator
import com.lenta.bp18.repository.IRepoInMemoryHolder
import com.lenta.bp18.repository.RepoInMemoryHolder
import com.lenta.bp18.request.FastResourcesMultiRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.functional.Either
import com.lenta.shared.platform.app_update.AppUpdateChecker
import com.lenta.shared.platform.resources.ISharedStringResourceManager
import com.lenta.shared.requests.network.Auth
import com.lenta.shared.utilities.Logg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FastLoadingViewModel : CoreLoadingViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var failureInterpreter: IFailureInterpreter

    @Inject
    lateinit var fastResourcesNetRequest: FastResourcesMultiRequest

    @Inject
    lateinit var appUpdateChecker: AppUpdateChecker

    @Inject
    lateinit var repository: RepoInMemoryHolder

    @Inject
    lateinit var auth: Auth

    @Inject
    lateinit var appUpdateInstaller: AppUpdateInstaller

    @Inject
    lateinit var resourceManager: ISharedStringResourceManager

    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    @Inject
    lateinit var sessionInfo: ISessionInfo

    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    init {
        viewModelScope.launch {
            progress.value = true
            withContext(Dispatchers.IO) {
                repoInMemoryHolder.storesRequestResult?.markets?.find { it.number == sessionInfo.market }.let { market ->
                    val codeVersion = market?.version?.toIntOrNull()
                    Logg.d { "codeVersion for update: $codeVersion" }
                    if (codeVersion == null) {
                        Either.Right("")
                    } else {
                        appUpdateInstaller.checkNeedAndHaveUpdate(codeVersion)
                    }
                }
            }.either({
                Logg.e { "checkNeedAndHaveUpdate failure $it" }
                handleFailure(failure = it)
            }) { updateFileName ->
                Logg.d { "update fileName: $updateFileName" }
                if (updateFileName.isBlank()) {
                    getFastResources()
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

    private fun getFastResources() {
        viewModelScope.launch {
            fastResourcesNetRequest(null).either(::handleFailure, ::handleSuccess)
        }
    }

    override fun handleFailure(failure: Failure) {
        navigator.openSelectMarketScreen()
        navigator.openAlertScreen(failureInterpreter.getFailureDescription(failure).message)
        progress.postValue(false)
    }

    private fun handleSuccess(notUsed: Boolean) {
        progress.postValue(false)
        navigator.closeAllScreen()
        navigator.openSelectGoodScreen()
    }

    override fun clean() {
        progress.postValue(false)
    }

}