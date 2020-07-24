package com.lenta.movement.features.loading.fast

import androidx.lifecycle.MutableLiveData
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.movement.repos.IRepoInMemoryHolder
import com.lenta.movement.requests.network.FastResourcesMultiRequest
import com.lenta.movement.requests.network.StockLockRequestResult
import com.lenta.movement.requests.network.StockNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.fmp.resources.dao_ext.getAllowedWobAppVersion
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.platform.app_update.AppUpdateChecker
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.requests.network.ServerTime
import com.lenta.shared.requests.network.ServerTimeRequest
import com.lenta.shared.requests.network.ServerTimeRequestParam
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
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

    private val zmpUtz14V001: ZmpUtz14V001 by lazy { ZmpUtz14V001(hyperHive) }

    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    init {
        launchUITryCatch {
            progress.value = true
            serverTimeRequest(
                ServerTimeRequestParam(
                    sessionInfo.market
                        ?: ""
                )
            ).either(::handleFailure, ::handleSuccessServerTime)
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
        progress.value = false
    }

    private fun handleSuccess(stockLockRequestResult: StockLockRequestResult) {
        repoInMemoryHolder.stockLockRequestResult = stockLockRequestResult
        launchUITryCatch {
            if (appUpdateChecker.isNeedUpdate(withContext(Dispatchers.IO) {
                    return@withContext zmpUtz14V001.getAllowedWobAppVersion()
                })) {
                hyperHive.authAPI.unAuth()
                screenNavigator.closeAllScreen()
                screenNavigator.openLoginScreen()
                screenNavigator.openNeedUpdateScreen()
            } else {
                screenNavigator.openSelectionPersonnelNumberScreen()
            }
            progress.value = false
        }
    }

    override fun clean() {
        progress.postValue(false)
    }
}