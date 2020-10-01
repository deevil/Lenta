package com.lenta.bp10.features.good_information.marked

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lenta.bp10.features.good_information.base.BaseProductInfoViewModel
import com.lenta.bp10.models.MarkedGoodStampCollector
import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.bp10.models.task.ProcessMarkedGoodProductService
import com.lenta.bp10.models.task.TaskDescription
import com.lenta.bp10.requests.network.GoodInfoNetRequest
import com.lenta.bp10.requests.network.GoodInfoParams
import com.lenta.bp10.requests.network.pojo.MarkInfo
import com.lenta.bp10.requests.network.pojo.Property
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.actionByNumber
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.mapSkipNulls
import com.lenta.shared.utilities.extentions.toStringFormatted
import javax.inject.Inject

class MarkedInfoViewModel : BaseProductInfoViewModel(), PageSelectionListener {

    @Inject
    lateinit var markSearchDelegate: MarkSearchDelegate

    @Inject
    lateinit var goodInfoNetRequest: GoodInfoNetRequest


    /**
    Переменные
     */

    private val processMarkedGoodProductService: ProcessMarkedGoodProductService by lazy {
        processServiceManager.getWriteOffTask()!!.processMarkedGoodProduct(productInfo.value!!)!!
    }

    private val markedGoodStampCollector: MarkedGoodStampCollector by lazy {
        MarkedGoodStampCollector(processMarkedGoodProductService)
    }

    private val properties = MutableLiveData(listOf<Property>())

    val propertyList by lazy {
        properties.map { list ->
            list?.mapIndexed { index, property ->
                ItemMarkedGoodPropertyUi(
                        position = "${index + 1}",
                        propertyName = property.name,
                        value = property.value
                )
            }
        }
    }

    /**
    Кнопки нижнего тулбара
     */

    val rollBackEnabled: LiveData<Boolean> by lazy {
        countValue.mapSkipNulls { it > 0.0 }
    }

    /**
    Блок инициализации
     */

    init {
        launchUITryCatch {
            initMarkSearchDelegate()

            if (isSpecialMode.value == true) {
                initGoodProperties()
            }
        }
    }

    /**
    Методы
     */

    private fun initMarkSearchDelegate() {
        markSearchDelegate.init(
                isSpecialMode = isSpecialMode,
                tkNumber = getTaskDescription().tkNumber,
                updateProperties = this@MarkedInfoViewModel::updateProperties,
                handleScannedMark = this@MarkedInfoViewModel::handleScannedMark,
                handleScannedBox = this@MarkedInfoViewModel::handleScannedBox,
                productInfo = productInfo.value
        )
    }

    private suspend fun initGoodProperties() {
        navigator.showProgressLoadingData(::handleFailure)

        goodInfoNetRequest(GoodInfoParams(
                tkNumber = sessionInfo.market.orEmpty(),
                material = productInfo.value?.materialNumber?.takeLast(MATERIAL_LAST_COUNT).orEmpty()
        )).also {
            navigator.hideProgress()
        }.either(::handleFailure) { result ->
            updateProperties(result.properties.orEmpty())
        }
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickRollBack() {
        markedGoodStampCollector.rollback()
    }

    override fun handleProductSearchResult(scanInfoResult: ScanInfoResult?): Boolean {
        scanInfoResult?.let {
            if (it.productInfo.materialNumber == productInfo.value?.materialNumber) {
                return true
            }
        }
        onClickApply()
        return false
    }

    override fun getTaskDescription(): TaskDescription {
        return processMarkedGoodProductService.taskDescription
    }

    override fun getTaskRepo(): ITaskRepository {
        return processMarkedGoodProductService.taskRepository
    }

    override fun getProcessTotalCount(): Double {
        return processMarkedGoodProductService.getTotalCount()
    }

    override fun onClickAdd() {
        addGood()
    }

    override fun onClickApply() {
        addGood()
        processMarkedGoodProductService.apply()
        navigator.goBack()
    }

    override fun initCountLiveData(): MutableLiveData<String> {
        return markedGoodStampCollector.observeCount().map { it.toStringFormatted() }
    }

    override fun onBackPressed(): Boolean {
        if (markedGoodStampCollector.isNotEmpty()) {
            navigator.openConfirmationToBackNotEmptyStampsScreen {
                navigator.goBack()
            }
            return false
        }
        processMarkedGoodProductService.discard()
        return true
    }

    private fun addGood(): Boolean {
        countValue.value?.let { currentCount ->
            if (enabledApplyButton.value != true && currentCount != 0.0) {
                showNotPossibleSaveScreen()
                return false
            }

            if (currentCount != 0.0) {
                markedGoodStampCollector.processAll(getSelectedReason())
            }

            count.value = "0"
            requestFocusToQuantity.value = true

            return true
        }

        return false
    }

    override fun onScanResult(data: String) {
        actionByNumber(
                number = data,
                funcForShoes = ::actionForMark,
                funcForCigarettes = ::actionForMark,
                funcForCigaretteBox = { markWithoutTail ->
                    if (isSpecialMode.value == false) {
                        actionForCigaretteBox(markWithoutTail)
                    }
                },
                funcForNotValidFormat = { searchGood(data) }
        )
    }

    private fun actionForMark(markNumber: String) {
        if (!markedGoodStampCollector.isContainsStamp(markNumber)) {
            markSearchDelegate.requestMarkInfo(markNumber)
        } else {
            navigator.openAlertDoubleScanStamp()
        }
    }

    private fun actionForCigaretteBox(boxNumber: String) {
        if (!markedGoodStampCollector.isContainsBox(boxNumber)) {
            markSearchDelegate.requestPackInfo(boxNumber)
        } else {
            navigator.openAlertDoubleScanStamp()
        }
    }

    private fun searchGood(data: String) {
        if (addGood()) {
            searchProductDelegate.searchCode(data)
        }
    }

    private fun handleScannedMark(mark: String) {
        markedGoodStampCollector.addMark(
                material = productInfo.value?.materialNumber.orEmpty(),
                markNumber = mark,
                writeOffReason = getSelectedReason().code
        )
    }

    private fun handleScannedBox(boxNumber: String, marks: List<MarkInfo>) {
        markedGoodStampCollector.addMarks(
                boxNumber = boxNumber,
                material = productInfo.value?.materialNumber.orEmpty(),
                marks = marks,
                writeOffReason = getSelectedReason().code
        )
    }

    private fun updateProperties(properties: List<Property>) {
        this.properties.value = properties
    }

    fun onClickDamaged() {
        markedGoodStampCollector.addBadMark(
                material = productInfo.value?.materialNumber.orEmpty(),
                writeOffReason = getSelectedReason().code
        )
    }

    override fun updateCounter() {
        markedGoodStampCollector.onDataChanged()
    }

    companion object {
        private const val MATERIAL_LAST_COUNT = 6
    }

}