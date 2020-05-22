package com.lenta.bp7.features.loading.fast

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app_update.AppUpdateInstaller
import com.lenta.bp7.data.CheckType
import com.lenta.bp7.data.model.CheckData
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IDatabaseRepo
import com.lenta.bp7.repos.IRepoInMemoryHolder
import com.lenta.bp7.requests.network.FastResourcesMultiRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.functional.Either
import com.lenta.shared.platform.app_update.AppUpdateChecker
import com.lenta.shared.platform.resources.ISharedStringResourceManager
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.requests.network.*
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
    lateinit var auth: Auth
    @Inject
    lateinit var sessionInfo: ISessionInfo

    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    init {
        viewModelScope.launch {
            progress.value = true
            fastResourcesNetRequest(null).either(::handleFailure, ::handleSuccess)
        }
    }

    override fun handleFailure(failure: Failure) {
        navigator.openLoginScreen()
        navigator.openAlertScreen(failureInterpreter.getFailureDescription(failure).message)
        progress.postValue(false)
    }

    private fun handleSuccess(@Suppress("UNUSED_PARAMETER") b: Boolean) {
        navigator.openSelectMarketScreen()
        progress.value = false
    }

    override fun clean() {
        progress.postValue(false)
    }

}