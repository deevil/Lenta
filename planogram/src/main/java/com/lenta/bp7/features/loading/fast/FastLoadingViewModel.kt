package com.lenta.bp7.features.loading.fast

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IRepoInMemoryHolder
import com.lenta.bp7.requests.network.*
import com.lenta.bp7.requests.network.loader.ResourcesLoader
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.platform.app_update.AppUpdateChecker
import com.lenta.shared.requests.network.StoresRequest
import com.lenta.shared.requests.network.StoresRequestResult
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import javax.inject.Inject

class FastLoadingViewModel : CoreLoadingViewModel() {

    @Inject
    lateinit var hyperHive: HyperHive
    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var failureInterpreter: IFailureInterpreter
    @Inject
    lateinit var resourceLoader: ResourcesLoader
    @Inject
    lateinit var fastResourcesNetRequest: FastResourcesMultiRequest
    @Inject
    lateinit var storesNetRequest: StoresNetRequest
    @Inject
    lateinit var appUpdateChecker: AppUpdateChecker
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder
    @Inject
    lateinit var storesRequest: StoresRequest

    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    init {
        viewModelScope.launch {
            progress.value = true
            fastResourcesNetRequest(null).either(::handleFailure, ::loadStores)
            progress.value = false
        }
    }

    private fun loadStores(@Suppress("UNUSED_PARAMETER") b: Boolean) {
        viewModelScope.launch {
            storesRequest(null).either(::handleFailure, ::handleSuccess)
        }
    }

    override fun handleFailure(failure: Failure) {
        navigator.openLoginScreen()
        navigator.openAlertScreen(failureInterpreter.getFailureDescription(failure).message)
    }

    private fun handleSuccess(storesRequestResult: StoresRequestResult) {
        repoInMemoryHolder.storesRequestResult = storesRequestResult
        navigator.openSelectMarketScreen()
    }

    override fun clean() {
        progress.postValue(false)
    }
}