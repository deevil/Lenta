package com.lenta.bp15.features.loading

import androidx.lifecycle.MutableLiveData
import com.lenta.bp15.platform.navigation.IScreenNavigator
import com.lenta.bp15.repository.IDatabaseRepository
import com.lenta.bp15.request.FastResourcesMultiRequest
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.platform.app_update.AppUpdateChecker
import com.lenta.shared.requests.network.Auth
import com.lenta.shared.utilities.extentions.launchUITryCatch
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
    lateinit var repository: IDatabaseRepository
    @Inject
    lateinit var auth: Auth


    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    init {
        launchUITryCatch {
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
        launchUITryCatch {
            if (appUpdateChecker.isNeedUpdate(repository.getAllowedAppVersion())) {
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