package com.lenta.movement.features.selectmarket

import androidx.lifecycle.MutableLiveData
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.movement.repos.IRepoInMemoryHolder
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.features.printer_change.PrinterManager
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.unsafeLazy
import com.lenta.shared.view.OnPositionClickListener
import javax.inject.Inject

class SelectMarketViewModel : CoreViewModel(), OnPositionClickListener {
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var appSettings: IAppSettings

    @Inject
    lateinit var printerManager: PrinterManager


    private val markets: MutableLiveData<List<MarketUi>> = MutableLiveData()
    val marketsNames: MutableLiveData<List<String>> = markets.map { markets ->
        markets?.map { it.number }
    }
    val selectedPosition: MutableLiveData<Int> = MutableLiveData()
    val selectedAddress: MutableLiveData<String> by unsafeLazy {
        selectedPosition.map {
            it?.let { position ->
                markets.value?.getOrNull(position)?.address
            }
        }
    }

    init {
        launchUITryCatch {
            repoInMemoryHolder.storesRequestResult?.let { storesRequestResult ->
                val listOfMarkets = storesRequestResult.markets.map {
                    MarketUi(number = it.number, address = it.address)
                }
                markets.value = listOfMarkets
                if (selectedPosition.value == null) {
                    if (appSettings.lastTK != null) {
                        for (i in listOfMarkets.indices) {
                            if (listOfMarkets[i].number == appSettings.lastTK) {
                                onClickPosition(i)
                                break
                            }
                        }
                    } else {
                        onClickPosition(0)
                    }
                }

                if (listOfMarkets.size == 1) {
                    onClickNext()
                }

            }
        }
    }


    fun onClickNext() {
        launchUITryCatch {
            markets.value?.getOrNull(selectedPosition.value ?: -1)?.number?.let { tkNumber ->
                if (appSettings.lastTK != tkNumber) {
                    printerManager.setDefaultPrinterForTk(tkNumber)
                }
                sessionInfo.market = tkNumber
                appSettings.lastTK = tkNumber
            }
            screenNavigator.openFastDataLoadingScreen()
        }
    }

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }
}

data class MarketUi(
        val number: String,
        val address: String
)