package com.lenta.bp18.features.select_market

import android.content.Context
import androidx.lifecycle.MutableLiveData
import app_update.AppUpdateInstaller
import com.lenta.bp18.R
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
import com.lenta.shared.requests.combined.scan_info.pojo.MarketInfo
import com.lenta.shared.requests.network.ServerTime
import com.lenta.shared.requests.network.ServerTimeRequest
import com.lenta.shared.requests.network.ServerTimeRequestParam
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.Dispatchers
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
    private var currentSelectedMarket: String? = ""

    val selectedPosition: MutableLiveData<Int> = MutableLiveData()

    val marketsNames: MutableLiveData<List<String>> = markets.map { markets ->
        markets?.map { it.number }
    }

    val selectedAddress: MutableLiveData<String> = selectedPosition.map {
        it?.let { position ->
            val market = markets.value?.getOrNull(position)
            currentSelectedMarket = market?.number.orEmpty()
            market?.address.orEmpty()
        }
    }

    init {
        launchUITryCatch {
            /**Выполнение запроса получения номера ТК*/
            marketOverIPRequest(MarketInfoParams(
                    ipAdress = context.getDeviceIp(),
                    mode = MODE_1
            )).either(::handleFailure, ::onResultHandler)
        }
    }


    fun onClickNext() {
        launchUITryCatch {
            navigator.showProgressLoadingData()

            /**Загрузка медленных справочников*/
            slowResourcesMultiRequest(null).either(::handleFailure)

            /**Выполнение запроса сохранения номера ТК*/
            marketOverIPRequest(MarketInfoParams(
                    ipAdress = context.getDeviceIp(),
                    mode = MODE_2,
                    werks = currentSelectedMarket
            ))

            applySelectedPosition()
        }
    }

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }

    private suspend fun applySelectedPosition() {
        val selectedPositionIndex = selectedPosition.value ?: 0
        markets.value
                ?.getOrNull(selectedPositionIndex)
                ?.number
                ?.let { tkNumber ->
                    processSelectedMarket(tkNumber)
                }
                ?: handleFailure(failure = Failure.MessageFailure(
                        messageResId = R.string.error_market_selected_position
                ))
    }

    private suspend fun processSelectedMarket(tkNumber: String) {
        withContext(Dispatchers.IO) {
            applyTkSelectedNumber(tkNumber)
            // ищем входной маркет
            val market = database.getMarketByNumber(tkNumber)
            checkNeedUpdateOrGoNext(market)

        }.either(::handleFailure, ::checkFileNameOrGetServerTime)
    }

    private suspend fun checkNeedUpdateOrGoNext(marketInfo: MarketInfo?): Either<Failure, String> {
        return marketInfo?.version?.toIntOrNull()?.let { codeVersion ->
            Logg.d { "codeVersion for update: $codeVersion" }
            appUpdateInstaller.checkNeedAndHaveUpdate(codeVersion)
        } ?: Either.Right("")
    }

    private fun onResultHandler(result: MarketInfoResult) = launchUITryCatch {
        val list = withContext(Dispatchers.IO) {
            database.getAllMarkets()
                    .map {
                        MarketUI(
                                number = it.number,
                                address = it.address
                        )
                    }
        }
        markets.value = list

        if (selectedPosition.value == null) {
            findSavedIndex(list)
        }

        if (list.size == 1) {
            onClickNext()
        }
    }

    private fun findSavedIndex(marketList: List<MarketUI>) {
        val marketToFind = appSettings.lastTK ?: currentSelectedMarket.orEmpty()
        if (marketToFind.isNotEmpty()) {
            marketList.find { it.number == appSettings.lastTK }?.let { market ->
                currentSelectedMarket = market.number
                val index = marketList.indexOf(market)
                onClickPosition(index)
            }
        }
    }

    private fun checkFileNameOrGetServerTime(updateFileName: String) {
        Logg.d { "update fileName: $updateFileName" }
        if (updateFileName.isNotEmpty()) {
            installUpdate(updateFileName)
        } else {
            getServerTime()
        }
    }

    private fun applyTkSelectedNumber(tkNumber: String) {
        if (appSettings.lastTK != tkNumber) {
            clearPrinters()
        }
        sessionInfo.market = tkNumber
        appSettings.lastTK = tkNumber
    }

    private fun getServerTime() = launchUITryCatch {
        serverTimeRequest(ServerTimeRequestParam(sessionInfo.market
                .orEmpty())).either(::handleFailure, ::handleSuccessServerTime)
    }

    private fun handleSuccessServerTime(serverTime: ServerTime) {
        navigator.hideProgress()
        timeMonitor.setServerTime(time = serverTime.time, date = serverTime.date)
        navigator.openSelectGoodScreen()
    }

    override fun handleFailure(failure: Failure) {
        Logg.e { "handleFailure failure: $failure" }
        navigator.openSelectMarketScreen()
        navigator.openAlertScreen(failureInterpreter.getFailureDescription(failure).message)
        navigator.hideProgress()
    }

    private fun installUpdate(updateFileName: String) {
        launchUITryCatch {
            navigator.showProgress(resourceManager.loadingNewAppVersion())
            withContext(Dispatchers.IO) {
                appUpdateInstaller.installUpdate(updateFileName)
            }.either(::handleFailure)
        }
    }

    private fun clearPrinters() {
        appSettings.printer = null
        sessionInfo.printer = null
    }

    companion object {
        /**Получить данные*/
        const val MODE_1 = "1"

        /**Отправить данные*/
        const val MODE_2 = "2"
    }
}