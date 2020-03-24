package com.lenta.bp9.features.select_market

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.network.MarketOverIPParams
import com.lenta.bp9.requests.network.MarketOverIPRequest
import com.lenta.bp9.requests.network.MarketOverIPRestInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.features.printer_change.PrinterManager
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class SelectMarketViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var appSettings: IAppSettings
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder
    @Inject
    lateinit var marketOverIPRequest: MarketOverIPRequest
    @Inject
    lateinit var printerManager: PrinterManager

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
    private val marketOverIP: MutableLiveData<String> = MutableLiveData()
    val deviceIp: MutableLiveData<String> = MutableLiveData()

    init {
        viewModelScope.launch {
            repoInMemoryHolder.permissions?.markets?.let { list ->

                markets.value = list.map { MarketUI(number = it.number, address = it.address) }

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
            /**screenNavigator.showProgressLoadingData()

            repoInMemoryHolder.permissions?.markets?.let { list ->

                markets.value = list.map { MarketUI(number = it.number, address = it.address) }

                if (list.size == 1) {
                    onClickNext()
                } else {
                    marketOverIPRequest(
                            MarketOverIPParams(
                                    ipAdress = deviceIp.value ?: "0.0.0.0",
                                    mode = "1",
                                    werks = "")).either(::handleFailure, ::handleMarketOverIPSuccess)
                }
            }
            screenNavigator.hideProgress()*/
        }
    }

    private fun handleMarketOverIPSuccess(marketOverIPRestInfo: MarketOverIPRestInfo) {
        marketOverIP.value = marketOverIPRestInfo.marketNumber
        repoInMemoryHolder.permissions?.markets?.let { list ->

            list.forEachIndexed { index, market ->
                if (market.number == marketOverIPRestInfo.marketNumber) {
                    onClickPosition(index)
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
            }
            screenNavigator.openFastDataLoadingScreen()

            /**screenNavigator.showProgressLoadingData()
            markets.value?.getOrNull(selectedPosition.value ?: -1)?.number?.let { tkNumber ->
                if (appSettings.lastTK != tkNumber) {
                    printerManager.setDefaultPrinterForTk(tkNumber)
                }
                sessionInfo.market = tkNumber
                appSettings.lastTK = tkNumber
                if (tkNumber != marketOverIP.value) {

                    marketOverIPRequest(
                            MarketOverIPParams(
                                    ipAdress = deviceIp.value ?: "0.0.0.0",
                                    mode = "2",
                                    werks = tkNumber)).either(::handleFailure) { }
                }
            }
            screenNavigator.hideProgress()
            screenNavigator.openFastDataLoadingScreen()*/
        }
    }


    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }
}


