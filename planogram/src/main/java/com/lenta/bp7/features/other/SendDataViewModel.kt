package com.lenta.bp7.features.other

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.CheckType
import com.lenta.bp7.data.model.CheckData
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IDatabaseRepo
import com.lenta.bp7.requests.network.SaveCheckDataParams
import com.lenta.bp7.requests.network.SaveCheckDataRestInfo
import com.lenta.bp7.requests.network.SaveExternalAuditDataNetRequest
import com.lenta.bp7.requests.network.SaveSelfControlDataNetRequest
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class SendDataViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var database: IDatabaseRepo
    @Inject
    lateinit var checkData: CheckData
    @Inject
    lateinit var saveSelfControlDataNetRequest: SaveSelfControlDataNetRequest
    @Inject
    lateinit var saveExternalAuditDataNetRequest: SaveExternalAuditDataNetRequest


    val marketIp: MutableLiveData<String> = MutableLiveData("")
    val terminalId: MutableLiveData<String> = MutableLiveData("")

    fun sendCheckResult() {
        viewModelScope.launch {
            val saveCheckDataParams = SaveCheckDataParams(
                    shop = checkData.getFormattedMarketNumber(),
                    terminalId = terminalId.value ?: "Not found!",
                    data = checkData.prepareXmlCheckResult(marketIp.value ?: "Not found!"),
                    saveDoc = 1)

            val saveRequestType = when (checkData.checkType) {
                CheckType.SELF_CONTROL -> saveSelfControlDataNetRequest
                CheckType.EXTERNAL_AUDIT -> saveExternalAuditDataNetRequest
            }

            saveRequestType.let { saveRequest ->
                navigator.showProgress(saveRequest)
                saveRequest.run(saveCheckDataParams).either(::handleDataSendingError, ::handleDataSendingSuccess)
                navigator.hideProgress()
            }
        }
    }

    private fun handleDataSendingError(failure: Failure) {
        // Сообщение - Ошибка сохранения в LUA
        navigator.showErrorSavingToLua {
            navigator.openSegmentListScreen()
        }
    }

    private fun handleDataSendingSuccess(saveCheckDataRestInfo: SaveCheckDataRestInfo) {
        // Сообщение - Успешно сохранено в LUA
        navigator.showSuccessfullySavedToLua {
            checkData.removeAllFinishedSegments()
            navigator.openSegmentListScreen()
        }
    }

}