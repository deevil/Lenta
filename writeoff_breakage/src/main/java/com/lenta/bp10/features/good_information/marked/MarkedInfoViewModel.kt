package com.lenta.bp10.features.good_information.marked

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.features.good_information.base.BaseProductInfoViewModel
import com.lenta.bp10.models.MarkedGoodStampCollector
import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.bp10.models.task.ProcessMarkedGoodProductService
import com.lenta.bp10.models.task.TaskDescription
import com.lenta.bp10.models.task.WriteOffReason
import com.lenta.bp10.repos.DatabaseRepository
import com.lenta.bp10.requests.network.pojo.MarkInfo
import com.lenta.bp10.requests.network.pojo.Property
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.actionByNumber
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import javax.inject.Inject

class MarkedInfoViewModel : BaseProductInfoViewModel(), PageSelectionListener {

    @Inject
    lateinit var markSearchDelegate: MarkSearchDelegate

    @Inject
    lateinit var database: DatabaseRepository


    /**
    Переменные
     */

    private val processMarkedGoodProductService: ProcessMarkedGoodProductService by lazy {
        processServiceManager.getWriteOffTask()!!.processMarkedGoodProduct(productInfo.value!!)!!
    }

    private val markedGoodStampCollector: MarkedGoodStampCollector by lazy {
        MarkedGoodStampCollector(processMarkedGoodProductService)
    }

    val isSpecialMode = MutableLiveData(false)

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
        markedGoodStampCollector.isCanBeRollback.map { it }
    }

    /**
    Блок инициализации
     */

    init {
        launchUITryCatch {
            val taskType = processMarkedGoodProductService.taskDescription.taskType.code
            isSpecialMode.value = database.isSpecialMode(taskType)

            markSearchDelegate.init(
                    tkNumber = getTaskDescription().tkNumber,
                    updateProperties = this@MarkedInfoViewModel::updateProperties,
                    handleScannedMark = this@MarkedInfoViewModel::handleScannedMark,
                    handleScannedBox = this@MarkedInfoViewModel::handleScannedBox,
                    productInfo = productInfo.value
            )
        }
    }

    /**
    Методы
     */

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
        screenNavigator.goBack()
    }

    override fun initCountLiveData(): MutableLiveData<String> {
        return markedGoodStampCollector.observeCount().map { it.toStringFormatted() }
    }

    override fun onBackPressed(): Boolean {
        if (markedGoodStampCollector.isNotEmpty()) {
            screenNavigator.openConfirmationToBackNotEmptyStampsScreen {
                screenNavigator.goBack()
            }
            return false
        }
        processMarkedGoodProductService.discard()
        return true
    }

    private fun addGood(): Boolean {
        countValue.value?.let {
            if (enabledApplyButton.value != true && it != 0.0) {
                showNotPossibleSaveScreen()
                return false
            }

            if (it != 0.0) {
                markedGoodStampCollector.processAll(getSelectedReason())
            }

            count.value = ""

            return true
        }

        return false
    }

    override fun onScanResult(data: String) {
        actionByNumber(
                number = data,
                funcForShoes = { _, markWithoutTail -> actionForMark(markWithoutTail) },
                funcForCigarettes = ::actionForMark,
                funcForCigaretteBox = { boxNumber ->
                    if (isSpecialMode.value == false) {
                        actionForCigaretteBox(boxNumber)
                    }
                },
                funcForNotValidFormat = { searchGood(data) }
        )
    }

    private fun actionForMark(markNumber: String) {
        if (!markedGoodStampCollector.isContainsStamp(markNumber)) {
            markSearchDelegate.requestMarkInfo(markNumber)
        } else {
            screenNavigator.openAlertDoubleScanStamp()
        }
    }

    private fun actionForCigaretteBox(boxNumber: String) {
        if (!markedGoodStampCollector.isContainsBox(boxNumber)) {
            markSearchDelegate.requestPackInfo(boxNumber)
        } else {
            screenNavigator.openAlertDoubleScanStamp()
        }
    }

    private fun searchGood(data: String) {
        if (addGood()) {
            searchProductDelegate.searchCode(data, fromScan = true)
        }
    }

    private fun handleScannedMark(mark: String) {
        markedGoodStampCollector.addMark(
                material = productInfo.value?.materialNumber.orEmpty(),
                markNumber = mark,
                writeOffReason = getSelectedReason().code
        )
    }

    private fun handleScannedBox(marks: List<MarkInfo>) {
        markedGoodStampCollector.addMarks(
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

}