package com.lenta.bp7.features.select_market

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.CheckType
import com.lenta.bp7.data.IPersistCheckResult
import com.lenta.bp7.data.model.CheckData
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IRepoInMemoryHolder
import com.lenta.shared.account.ISessionInfo
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
    lateinit var checkData: CheckData
    @Inject
    lateinit var persistCheckResult: IPersistCheckResult


    private val markets: MutableLiveData<List<MarketUi>> = MutableLiveData()
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
                markets.value = list.map { MarketUi(number = it.number, address = it.address) }

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
        markets.value?.getOrNull(selectedPosition.value ?: -1)?.number?.let {
            if (appSettings.lastTK != it) {
                clearPrinters()
            }
            sessionInfo.printer = appSettings.printer
            sessionInfo.market = it
            appSettings.lastTK = it

            viewModelScope.launch {
                navigator.showProgress(serverTimeRequest)
                serverTimeRequest(ServerTimeRequestParam(sessionInfo.market
                        ?: "")).either(::handleFailure, ::handleSuccessServerTime)
                navigator.hideProgress()
            }
        }
    }

    private fun handleSuccessServerTime(serverTime: ServerTime) {
        timeMonitor.setServerTime(time = serverTime.time, date = serverTime.date)
        checkData.marketNumber = sessionInfo.market ?: "Not found!"

        // Раскомментировать для удаление сохраненных данных
        //persistCheckResult.clearSavedData()

        val savedResult = persistCheckResult.getSavedCheckResult()
        if (savedResult != null) {
            checkData.restoreSavedCheckResult(savedResult)

            when (checkData.checkType) {
                CheckType.SELF_CONTROL -> {
                    // Подтверждение - На устройстве обнаружены несохраненные данные в режиме "Самоконтроль ТК" - Назад / Перейти
                    navigator.showUnsavedSelfControlDataDetected {
                        navigator.openCodeScreen()
                    }
                }
                CheckType.EXTERNAL_AUDIT -> {
                    // Подтверждение - На устройстве обнаружены несохраненные данные в режиме "Внешний аудит" - Назад / Перейти
                    navigator.showUnsavedExternalAuditDataDetected {
                        navigator.openCodeScreen()
                    }
                }
            }
        } else {
            navigator.openSelectCheckTypeScreen()
        }
    }

    private fun clearPrinters() {
        appSettings.printer = null
        sessionInfo.printer = null
    }

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }
}

data class MarketUi(
        val number: String,
        val address: String
)