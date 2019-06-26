package com.lenta.bp10.features.loading.fast

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.requests.network.FastResourcesMultiRequest
import com.lenta.bp10.requests.network.StoresNetRequest
import com.lenta.bp10.requests.network.loader.ResourcesLoader
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.fmp.resources.dao_ext.getAllowedWobAppVersion
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.platform.app_update.AppUpdateChecker
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FastLoadingViewModel : CoreLoadingViewModel() {

    @Inject
    lateinit var hyperHive: HyperHive
    @Inject
    lateinit var fastResourcesNetRequest: FastResourcesMultiRequest
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var failureInterpreter: IFailureInterpreter
    @Inject
    lateinit var resourceLoader: ResourcesLoader
    @Inject
    lateinit var storesNetRequest: StoresNetRequest
    @Inject
    lateinit var appUpdateChecker: AppUpdateChecker

    val zmpUtz14V001: ZmpUtz14V001 by lazy { ZmpUtz14V001(hyperHive) }

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
            storesNetRequest(null).either(::handleFailure, ::handleSuccess)
        }

    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openLoginScreen()
        screenNavigator.openAlertScreen(failureInterpreter.getFailureDescription(failure).message)
    }

    private fun handleSuccess(@Suppress("UNUSED_PARAMETER") b: Boolean) {
        viewModelScope.launch {
            if (appUpdateChecker.isNeedUpdate(withContext(Dispatchers.IO) {
                        return@withContext zmpUtz14V001.getAllowedWobAppVersion()
                    })) {

                hyperHive.authAPI.unAuth()
                screenNavigator.closeAllScreen()
                screenNavigator.openLoginScreen()
                screenNavigator.openNeedUpdateScreen()
            } else {
                resourceLoader.startLoadSlowResources()
                screenNavigator.openSelectionPersonnelNumberScreen()
            }
        }

    }

    override fun clean() {
        progress.postValue(false)
    }

}