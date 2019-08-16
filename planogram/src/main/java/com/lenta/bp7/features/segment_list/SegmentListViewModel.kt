package com.lenta.bp7.features.segment_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.CheckType
import com.lenta.bp7.data.model.CheckData
import com.lenta.bp7.data.model.Segment
import com.lenta.bp7.data.model.SegmentStatus
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IDatabaseRepo
import com.lenta.bp7.requests.network.SaveCheckDataParams
import com.lenta.bp7.requests.network.SaveCheckDataRestInfo
import com.lenta.bp7.requests.network.SaveExternalAuditDataNetRequest
import com.lenta.bp7.requests.network.SaveSelfControlDataNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.analytics.AnalyticsHelper
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class SegmentListViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var database: IDatabaseRepo
    @Inject
    lateinit var checkData: CheckData
    @Inject
    lateinit var saveSelfControlDataNetRequest: SaveSelfControlDataNetRequest
    @Inject
    lateinit var saveExternalAuditDataNetRequest: SaveExternalAuditDataNetRequest
    @Inject
    lateinit var analyticsHelper: AnalyticsHelper


    companion object {
        const val SEGMENT_NUMBER_LENGTH = 7
    }

    val segments: MutableLiveData<List<Segment>> = MutableLiveData()

    val marketIp: MutableLiveData<String> = MutableLiveData("")
    val terminalId: MutableLiveData<String> = MutableLiveData("")

    val marketNumber: MutableLiveData<String> = MutableLiveData("")
    val segmentNumber: MutableLiveData<String> = MutableLiveData("")
    val requestFocusToSegmentNumber: MutableLiveData<Boolean> = MutableLiveData(true)

    val completeButtonEnabled: MutableLiveData<Boolean> = segments.map { segments ->
        segments?.isNotEmpty() ?: false && if (segments?.size == 1) segments[0].getStatus() != SegmentStatus.UNFINISHED else true
    }

    init {
        viewModelScope.launch {
            marketNumber.value = sessionInfo.market
            segments.value = checkData.segments
        }
    }

    fun updateSegmentList() {
        segments.value = checkData.segments
    }

    fun saveCheckResultToDatabase() {
        checkData.saveCheckResult()
    }

    override fun onOkInSoftKeyboard(): Boolean {
        if (checkData.isExistUnfinishedSegment()) {
            // Подтверждение - Обнаружен незавершенный сегмент. Начало работы с новым сегментом невозможно. Перейти к обработке сегмента? - Назад / Перейти
            navigator.showIncompleteSegmentDetected {
                checkData.setUnfinishedSegmentAsCurrent()
                navigator.openShelfListScreen()
            }
        } else {
            checkNumber()
        }

        return true
    }

    private fun checkNumber() {
        segmentNumber.value?.let { number ->
            if (number.length == SEGMENT_NUMBER_LENGTH) {
                val segment = segments.value?.find { it.number == number }
                if (segment != null) {
                    if (segment.getStatus() == SegmentStatus.DELETED) {
                        // Выбор - Сегмент удален. Открыть просмотр или создать новый? - Назад / Просмотр / Создать
                        navigator.showSegmentIsDeleted(
                                reviewCallback = { openExistSegment(segment) },
                                createCallback = { createSegment(number) })
                    } else {
                        openExistSegment(segment)
                    }
                } else {
                    createSegment(number)
                }
            }
        }
    }

    private fun openExistSegment(segment: Segment) {
        checkData.currentSegmentIndex = segments.value!!.indexOf(segment)
        navigator.openShelfListScreen()
    }

    private fun createSegment(segmentNumber: String) {
        // Сообщение - Начата обработка сегмента
        navigator.showSegmentStarted(
                segmentNumber = segmentNumber,
                isFacings = checkData.countFacings) {
            checkData.addSegment(sessionInfo.market ?: "Not found!", segmentNumber)
            navigator.openShelfListScreen()
        }
    }

    fun onClickComplete() {
        saveCheckResult()
    }

    private fun saveCheckResult() {
        viewModelScope.launch {
            val checkResult = checkData.prepareXmlCheckResult(marketIp.value ?: "Not found!")
            analyticsHelper.logXmlCheckResult(checkResult)

            val saveCheckDataParams = SaveCheckDataParams(
                    shop = checkData.getFormattedMarketNumber(),
                    terminalId = terminalId.value ?: "Not found!",
                    data = checkResult,
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
        // Сообщение - Ошибка сохранения в LAU
        navigator.showErrorSavingToLua {
            navigator.openSegmentListScreen()
        }
    }

    private fun handleDataSendingSuccess(saveCheckDataRestInfo: SaveCheckDataRestInfo) {
        // Сообщение - Успешно сохранено в LAU
        navigator.showSuccessfullySavedToLua {
            checkData.removeAllFinishedSegments()
            navigator.openSegmentListScreen()
        }
    }

    fun onClickItemPosition(position: Int) {
        checkData.currentSegmentIndex = position
        navigator.openShelfListScreen()
    }

    fun onClickBack() {
        navigator.openSelectCheckTypeScreen()
    }

}
