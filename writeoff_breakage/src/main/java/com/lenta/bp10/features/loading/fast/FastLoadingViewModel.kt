package com.lenta.bp10.features.loading.fast

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.requests.network.FastResourcesMultiRequest
import com.lenta.bp10.requests.network.loader.ResourcesLoader
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.loading.CoreLoadingViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class FastLoadingViewModel : CoreLoadingViewModel() {
    @Inject
    lateinit var fastResourcesNetRequest: FastResourcesMultiRequest
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var failureInterpreter: IFailureInterpreter
    @Inject
    lateinit var resourceLoader: ResourcesLoader

    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    init {
        viewModelScope.launch {
            progress.value = true
            fastResourcesNetRequest(null).either(::handleFailure, ::handleSuccess)
            progress.value = false
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failureInterpreter.getFailureDescription(failure))
    }

    private fun handleSuccess(@Suppress("UNUSED_PARAMETER") b: Boolean) {
        resourceLoader.startLoadSlowResources()
        screenNavigator.openSelectionTabNumberScreen()
    }

    override fun clean() {
        progress.postValue(false)
    }

}