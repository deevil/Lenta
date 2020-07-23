package com.lenta.bp7.features.select_market

import androidx.lifecycle.MutableLiveData
import app_update.AppUpdateInstaller
import com.lenta.bp7.data.CheckType
import com.lenta.bp7.data.model.CheckData
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IDatabaseRepo
import com.lenta.bp7.repos.IRepoInMemoryHolder
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
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
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import com.mobrun.plugin.api.HyperHive
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
    lateinit var hyperHive: HyperHive
    @Inject
    lateinit var serverTimeRequest: ServerTimeRequest
    @Inject
    lateinit var timeMonitor: ITimeMonitor
    @Inject
    lateinit var checkData: CheckData
    @Inject
    lateinit var failureInterpreter: IFailureInterpreter
    @Inject
    lateinit var appUpdateInstaller: AppUpdateInstaller
    @Inject
    lateinit var resourceManager: ISharedStringResourceManager
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder


    private val markets: MutableLiveData<List<MarketInfo>> = MutableLiveData()
    val marketsNames: MutableLiveData<List<String>> = markets.map { markets ->
        markets?.map { it.number }
    }
    val selectedPosition: MutableLiveData<Int> = MutableLiveData()
    val selectedAddress: MutableLiveData<String> = selectedPosition.map {
        it?.let { position -> markets.value?.getOrNull(position)?.address }
    }
    val title: MutableLiveData<String> = MutableLiveData()

    init {
        launchUITryCatch {
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
        launchUITryCatch {
            navigator.showProgressLoadingData(::handleFailure)
            markets.value?.getOrNull(selectedPosition.value ?: -1)?.number?.let { tkNumber ->
                if (appSettings.lastTK != tkNumber) {
                    clearPrinters()
                }
                sessionInfo.market = tkNumber
                appSettings.lastTK = tkNumber

                withContext(Dispatchers.IO) {
                    database.getAllMarkets().find { it.number == sessionInfo.market }.let { market ->
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
        launchUITryCatch {
            serverTimeRequest(ServerTimeRequestParam(sessionInfo.market
                   .orEmpty())).either(::handleFailure, ::handleSuccessServerTime)
        }
    }

    private fun handleSuccessServerTime(serverTime: ServerTime) {
        navigator.hideProgress()
        timeMonitor.setServerTime(time = serverTime.time, date = serverTime.date)
        checkData.marketNumber = sessionInfo.market.orEmpty()

        // Раскомментировать для удаление сохраненных данных
        //checkData.clearSavedData()

        if (checkData.isExistUnsentData()) {
            when (checkData.checkType) {
                CheckType.SELF_CONTROL -> {
                    // Подтверждение - На устройстве обнаружены несохраненные данные в режиме "Самоконтроль ТК" - Назад / Перейти
                    navigator.showUnsavedSelfControlDataDetected {
                        navigator.openSelectCheckTypeScreen()
                    }
                }
                CheckType.EXTERNAL_AUDIT -> {
                    // Подтверждение - На устройстве обнаружены несохраненные данные в режиме "Внешний аудит" - Назад / Перейти
                    navigator.showUnsavedExternalAuditDataDetected {
                        navigator.openSelectCheckTypeScreen()
                    }
                }
            }
        } else {
            navigator.openSelectCheckTypeScreen()
        }
    }

    override fun handleFailure(failure: Failure) {
        navigator.openLoginScreen()
        navigator.openAlertScreen(failureInterpreter.getFailureDescription(failure).message)
        navigator.hideProgress()
    }

    private fun installUpdate(updateFileName: String) {
        launchUITryCatch {
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

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }

}