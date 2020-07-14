package com.lenta.bp18.features.select_market

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lenta.bp18.model.pojo.MarketUI
import com.lenta.bp18.platform.navigation.IScreenNavigator
import com.lenta.bp18.repository.IRepoInMemoryHolder
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.printer_change.PrinterManager
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.network.ServerTime
import com.lenta.shared.requests.network.ServerTimeRequest
import com.lenta.shared.requests.network.ServerTimeRequestParam
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class SelectMarketViewModel : CoreViewModel(), OnPositionClickListener {
    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var appSettings: IAppSettings

    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    @Inject
    lateinit var timeMonitor: ITimeMonitor

    @Inject
    lateinit var serverTimeRequest: ServerTimeRequest

    @Inject
    lateinit var printerManager: PrinterManager

    @Inject
    lateinit var gson: Gson

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

    init {
        viewModelScope.launch {
            repoInMemoryHolder.storesRequestResult?.markets?.let { list ->
                markets.value = list.map {
                    MarketUI(number = it.number, address = it.address)
                }

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
            markets.value?.getOrNull(selectedPosition.value ?: -1)?.number?.let { tkNumber ->
                if (appSettings.lastTK != tkNumber) {
                    printerManager.setDefaultPrinterForTk(tkNumber)
                }
                sessionInfo.market = tkNumber
                appSettings.lastTK = tkNumber
                navigator.showProgress(serverTimeRequest)
                serverTimeRequest(ServerTimeRequestParam(sessionInfo.market
                        .orEmpty())).either(::handleFailure, ::handleSuccessServerTime)
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
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
}