package com.lenta.bp16.features.loading.fast

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.repository.IDatabaseRepository
import com.lenta.bp16.request.FastResourcesMultiRequest
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.platform.app_update.AppUpdateChecker
import com.lenta.shared.requests.network.Auth
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
    lateinit var database: IDatabaseRepository
    @Inject
    lateinit var auth: Auth


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
        navigator.openLoginScreen()
        navigator.openAlertScreen(failureInterpreter.getFailureDescription(failure).message)
    }

    private fun handleSuccess(notUsed: Boolean) {
        viewModelScope.launch {
            if (appUpdateChecker.isNeedUpdate(database.getAllowedAppVersion())) {
                auth.cancelAuthorization()
                navigator.closeAllScreen()
                navigator.openLoginScreen()
                navigator.openNeedUpdateScreen()
            } else {
                navigator.openSelectMarketScreen()
            }
        }
    }

    override fun clean() {
        progress.postValue(false)
    }

}