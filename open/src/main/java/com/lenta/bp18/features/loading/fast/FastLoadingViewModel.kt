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
import com.lenta.shared.platform.resources.ISharedStringResourceManager
import com.lenta.shared.requests.network.Auth
import com.lenta.shared.utilities.extentions.launchUITryCatch
import kotlinx.coroutines.launch
import javax.inject.Inject

class FastLoadingViewModel : CoreLoadingViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var failureInterpreter: IFailureInterpreter
    @Inject
    lateinit var fastResourcesNetRequest: FastResourcesMultiRequest

    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    init {
        launchUITryCatch {
            progress.value = true
            fastResourcesNetRequest(null).either(::handleFailure, ::handleSuccess)
        }
    }

    override fun handleFailure(failure: Failure) {
        navigator.openAuthScreen()
        navigator.openAlertScreen(failureInterpreter.getFailureDescription(failure).message)
        progress.value = false
    }

    private fun handleSuccess(b: Boolean) {
        progress.value = false
        navigator.openSelectMarketScreen()
    }

    override fun clean() {
        progress.value = false
    }

}