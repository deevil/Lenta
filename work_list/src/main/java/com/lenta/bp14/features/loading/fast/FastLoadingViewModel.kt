package com.lenta.bp14.features.loading.fast

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.general.IGeneralRepo
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.repos.IRepoInMemoryHolder
import com.lenta.bp14.requests.FastResourcesMultiRequest
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.platform.app_update.AppUpdateChecker
import com.lenta.shared.requests.network.Auth
import com.lenta.shared.requests.network.StoresRequest
import com.lenta.shared.requests.network.StoresRequestResult
import kotlinx.coroutines.launch
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
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder
    @Inject
    lateinit var storesRequest: StoresRequest
    @Inject
    lateinit var generalRepo: IGeneralRepo
    @Inject
    lateinit var auth: Auth

    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    init {
        viewModelScope.launch {
            progress.value = true
            fastResourcesNetRequest(null).either(::handleFailure, ::loadStores)
        }
    }

    private fun loadStores(@Suppress("UNUSED_PARAMETER") b: Boolean) {
        viewModelScope.launch {
            progress.value = true
            storesRequest(null).either(::handleFailure, ::handleSuccess)
        }
    }

    override fun handleFailure(failure: Failure) {
        progress.value = false
        navigator.openLoginScreen()
        navigator.openAlertScreen(failureInterpreter.getFailureDescription(failure).message)
    }

    private fun handleSuccess(storesRequestResult: StoresRequestResult) {
        progress.value = true
        repoInMemoryHolder.storesRequestResult = storesRequestResult

        //TODO добавить проверку необходимости обновления

        /*viewModelScope.launch {
            if (appUpdateChecker.isNeedUpdate(database.getAllowedAppVersion())) {
                auth.cancelAuthorization()
                navigator.closeAllScreen()
                navigator.openLoginScreen()
                navigator.openNeedUpdateScreen()
            } else {
                navigator.openSelectMarketScreen()
            }
        }*/

        viewModelScope.launch {
            generalRepo.onDbReady()
            navigator.openSelectMarketScreen()
        }


    }

    override fun clean() {
        progress.postValue(false)
    }
}