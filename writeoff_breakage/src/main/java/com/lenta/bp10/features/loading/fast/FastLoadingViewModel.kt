package com.lenta.bp10.features.loading.fast

import androidx.lifecycle.MutableLiveData
import app_update.AppUpdateInstaller
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.repos.IRepoInMemoryHolder
import com.lenta.bp10.requests.network.FastResourcesMultiRequest
import com.lenta.bp10.requests.network.StockLockRequestResult
import com.lenta.bp10.requests.network.StockNetRequest
import com.lenta.bp10.requests.network.loader.ResourcesLoader
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.functional.Either
import com.lenta.shared.platform.app_update.AppUpdateChecker
import com.lenta.shared.platform.resources.ISharedStringResourceManager
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.requests.network.ServerTime
import com.lenta.shared.requests.network.ServerTimeRequest
import com.lenta.shared.requests.network.ServerTimeRequestParam
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers.IO
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
    lateinit var stockNetRequest: StockNetRequest

    @Inject
    lateinit var appUpdateChecker: AppUpdateChecker

    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    @Inject
    lateinit var serverTimeRequest: ServerTimeRequest

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var timeMonitor: ITimeMonitor

    @Inject
    lateinit var appUpdateInstaller: AppUpdateInstaller

    @Inject
    lateinit var resourceManager: ISharedStringResourceManager

    val zmpUtz14V001: ZmpUtz14V001 by lazy { ZmpUtz14V001(hyperHive) }

    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    init {
        launchUITryCatch {
            progress.value = true
            withContext(IO) {
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
        launchUITryCatch {
            title.value = resourceManager.loadingNewAppVersion()
            progress.value = true
            withContext(IO) {
                appUpdateInstaller.installUpdate(updateFileName)
            }.either(::handleFailure) {
                // do nothing. App is finished
            }
        }

    }

    private fun getServerTime() {
        launchUITryCatch {
            serverTimeRequest(ServerTimeRequestParam(sessionInfo.market.orEmpty()))
                    .either(::handleFailure, ::handleSuccessServerTime)
        }
    }

    private fun handleSuccessServerTime(serverTime: ServerTime) {
        timeMonitor.setServerTime(time = serverTime.time, date = serverTime.date)
        launchUITryCatch {
            fastResourcesNetRequest(null).either(::handleFailure, ::loadStocks)
        }
    }

    private fun loadStocks(@Suppress("UNUSED_PARAMETER") b: Boolean) {
        launchUITryCatch {
            stockNetRequest(null).either(::handleFailure, ::handleSuccess)
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openLoginScreen()
        screenNavigator.openAlertScreen(failureInterpreter.getFailureDescription(failure).message)
        progress.postValue(false)
    }

    private fun handleSuccess(stockLockRequestResult: StockLockRequestResult) {
        repoInMemoryHolder.stockLockRequestResult = stockLockRequestResult
        screenNavigator.openSelectionPersonnelNumberScreen(codeConfirmation = null, isScreenMainMenu = false)
        progress.value = false
    }

    override fun clean() {
        progress.postValue(false)
    }

}