package com.lenta.bp18.features.select_market

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app_update.AppUpdateInstaller
import com.lenta.bp18.model.pojo.MarketUI
import com.lenta.bp18.platform.navigation.IScreenNavigator
import com.lenta.bp18.repository.IDatabaseRepo
import com.lenta.bp18.request.SlowResourcesMultiRequest
import com.lenta.bp18.request.model.params.MarketInfoParams
import com.lenta.bp18.request.model.result.MarketInfoResult
import com.lenta.bp18.request.network.MarketOverIPRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.printer_change.PrinterManager
import com.lenta.shared.functional.Either
import com.lenta.shared.platform.resources.ISharedStringResourceManager
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.network.ServerTime
import com.lenta.shared.requests.network.ServerTimeRequest
import com.lenta.shared.requests.network.ServerTimeRequestParam
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.getDeviceId
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.launchUITryCatch
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
    lateinit var database: IDatabaseRepo

    @Inject
    lateinit var failureInterpreter: IFailureInterpreter

    @Inject
    lateinit var marketOverIPRequest: MarketOverIPRequest

    @Inject
    lateinit var timeMonitor: ITimeMonitor

    @Inject
    lateinit var serverTimeRequest: ServerTimeRequest

    @Inject
    lateinit var printerManager: PrinterManager

    @Inject
    lateinit var appUpdateInstaller: AppUpdateInstaller

    @Inject
    lateinit var resourceManager: ISharedStringResourceManager

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var slowResourcesMultiRequest: SlowResourcesMultiRequest

    private val markets: MutableLiveData<List<MarketUI>> = MutableLiveData()
    val marketsNames: MutableLiveData<List<String>> = markets.map { markets ->
        markets?.map { it.number }
    }

    val selectedPosition: MutableLiveData<Int> = MutableLiveData()

    val selectedAddress: MutableLiveData<String> = selectedPosition.map {
        it?.let { position ->
            markets.value?.getOrNull(position)?.address
        }
    }

    var currentMarket: String? = ""

    init {
        launchUITryCatch {

            /**Выполнение запроса получения номера ТК*/
            marketOverIPRequest(MarketInfoParams(
                    ipAdress = context.getDeviceIp(),
                    mode = MODE_1
            ))

            database.getAllMarkets().let { list ->

                markets.value = list.map { MarketUI(number = it.number, address = it.address) }

                if (selectedPosition.value == null) {

                    if (appSettings.lastTK != null) {
                        list.forEachIndexed { index, market ->
                            if (market.number == appSettings.lastTK) {
                                currentMarket = market.number
                                onClickPosition(index)
                            }
                        }
                    } else {
                        list.forEachIndexed { index, market ->
                            if (market.number == currentMarket)
                                currentMarket = market.number
                                onClickPosition(index)
                        }
                    }
                }

                if (list.size == 1) {
                    onClickNext()
                }
            }
        }
    }

    fun onClickNext() {
        launchUITryCatch {
            navigator.showProgressLoadingData()

            /**Загрузка медленных справочников*/
            slowResourcesMultiRequest(null).either(::handleFailure, ::handleSuccess)

            /**Выполнение запроса сохранения номера ТК*/
            marketOverIPRequest(MarketInfoParams(
                    ipAdress = context.getDeviceIp(),
                    mode = MODE_2,
                    werks = currentMarket
            ))

            markets.value
                    ?.getOrNull(selectedPosition.value ?: -1)?.number
                    ?.let { tkNumber ->
                        if (appSettings.lastTK != tkNumber) {
                            clearPrinters()
                        }
                        sessionInfo.market = tkNumber
                        appSettings.lastTK = tkNumber

                        withContext(Dispatchers.IO) {
                            val marketList = database.getAllMarkets()
                            val market = marketList.find { it.number == sessionInfo.market }
                            market?.let {
                                val codeVersion = it.version.toIntOrNull()
                                Logg.d { "codeVersion for update: $codeVersion" }
                                codeVersion?.run {
                                    appUpdateInstaller.checkNeedAndHaveUpdate(this)
                                } ?: Either.Right("")
                            }
                        }?.either({
                            Logg.e { "checkNeedAndHaveUpdate failure: $it" }
                            handleFailure(failure = it)
                        }) { updateFileName ->
                            Logg.d { "update fileName: $updateFileName" }
                            updateFileName.takeIf {
                                it.isNotBlank()
                            }?.let(::installUpdate) ?: getServerTime()
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

    private fun handleSuccessServerTime(serverTime: ServerTime) {
        navigator.hideProgress()
        timeMonitor.setServerTime(time = serverTime.time, date = serverTime.date)

        navigator.openSelectGoodScreen()

    }

    override fun handleFailure(failure: Failure) {
        navigator.openSelectMarketScreen()
        navigator.openAlertScreen(failureInterpreter.getFailureDescription(failure).message)
        navigator.hideProgress()
    }

    private fun handleSuccess(b: Boolean){
        //
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

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }


    private fun clearPrinters() {
        appSettings.printer = null
        sessionInfo.printer = null
    }

    companion object{
        /**Получить данные*/
        const val MODE_1 = "1"
        /**Отправить данные*/
        const val MODE_2 = "2"
    }
}