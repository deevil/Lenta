package com.lenta.bp18.features.select_market

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app_update.AppUpdateInstaller
import com.lenta.bp18.platform.navigation.IScreenNavigator
import com.lenta.bp18.repository.IDatabaseRepo
import com.lenta.bp18.repository.IRepoInMemoryHolder
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.printer_change.PrinterManager
import com.lenta.shared.functional.Either
import com.lenta.shared.platform.resources.ISharedStringResourceManager
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.MarketInfo
import com.lenta.shared.requests.network.ServerTime
import com.lenta.shared.requests.network.ServerTimeRequest
import com.lenta.shared.requests.network.ServerTimeRequestParam
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SelectMarketViewModel : CoreViewModel(), OnPositionClickListener {
    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var appSettings: IAppSettings

    @Inject
    lateinit var failureInterpreter: IFailureInterpreter

    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    @Inject
    lateinit var timeMonitor: ITimeMonitor

    @Inject
    lateinit var serverTimeRequest: ServerTimeRequest

    @Inject
    lateinit var printerManager: PrinterManager

    @Inject
    lateinit var database: IDatabaseRepo

    @Inject
    lateinit var appUpdateInstaller: AppUpdateInstaller

    @Inject
    lateinit var resourceManager: ISharedStringResourceManager

    private val markets: MutableLiveData<List<MarketInfo>> = MutableLiveData()

    val marketsNames: MutableLiveData<List<String>> = markets.map { markets ->
        markets?.map { it.number }
    }

    val selectedPosition: MutableLiveData<Int> = MutableLiveData()

    val selectedAddress: MutableLiveData<String> = selectedPosition.map {
        it?.let { position ->
            markets.value?.getOrNull(position)?.address
        }
    }
    val title: MutableLiveData<String> = MutableLiveData()

    init {
        viewModelScope.launch {
            database.getAllMarkets().let { list ->
                markets.value = list

                if (selectedPosition.value == null) {
                    if (appSettings.lastTK != null) {
                        list.forEachIndexed { index, market ->
                            if (market.number == appSettings.lastTK) {
                                onClickPosition(index)
                            }
                        }
                    } else {
                        onClickPosition(0)
                    }
                }

                if (list.size == 1) {
                    onClickNext()
                }
            }
        }
    }

    fun onClickNext() {
        viewModelScope.launch {
            navigator.showProgressLoadingData()
            markets.value
                    ?.getOrNull(selectedPosition.value ?: -1)?.number
                    ?.let { tkNumber ->
                        if (appSettings.lastTK != tkNumber) {
                            clearPrinters()
                        }
                        sessionInfo.market = tkNumber
                        appSettings.lastTK = tkNumber

                        withContext(Dispatchers.IO) {
                            database.getAllMarkets()
                                    .find { it.number == sessionInfo.market }
                                    .let { market ->
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
    }

    private fun getServerTime() {
        viewModelScope.launch {
            serverTimeRequest(ServerTimeRequestParam(sessionInfo.market
                    .orEmpty())).either(::handleFailure, ::handleSuccessServerTime)
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.openSelectMarketScreen()
        navigator.openAlertScreen(failureInterpreter.getFailureDescription(failure).message)
        navigator.hideProgress()
    }

    private fun handleSuccessServerTime(serverTime: ServerTime) {
        navigator.hideProgress()
        timeMonitor.setServerTime(time = serverTime.time, date = serverTime.date)
        navigator.openFastDataLoadingScreen()
    }

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }

    private fun installUpdate(updateFileName: String) {
        viewModelScope.launch {
            navigator.showProgress(resourceManager.loadingNewAppVersion())
            withContext(Dispatchers.IO) {
                appUpdateInstaller.installUpdate(updateFileName)
            }.either(::handleFailure) {
                // do nothing. App is finished
            }
        }
    }

    private fun clearPrinters() {
        appSettings.printer = null
        sessionInfo.printer = null
    }
}