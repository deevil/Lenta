package com.lenta.bp10.features.good_information.marked

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.features.good_information.base.BaseProductInfoViewModel
import com.lenta.bp10.models.MarkedGoodStampCollector
import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.bp10.models.task.ProcessMarkProductService
import com.lenta.bp10.models.task.TaskDescription
import com.lenta.bp10.models.task.WriteOffReason
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


    private val processMarkProductService: ProcessMarkProductService by lazy {
        processServiceManager.getWriteOffTask()!!.processMarkProduct(productInfo.value!!)!!
    }

    private val markedGoodStampCollector: MarkedGoodStampCollector by lazy {
        MarkedGoodStampCollector(processMarkProductService)
    }

    var selectedPage = MutableLiveData(0)

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

    val rollBackEnabled: LiveData<Boolean> by lazy {
        countValue.map { it ?: 0.0 > 0.0 }
    }

    init {
        launchUITryCatch {
            markSearchDelegate.init(
                    updateProperties = this@MarkedInfoViewModel::updateProperties,
                    viewModelScope = this@MarkedInfoViewModel::viewModelScope,
                    handleScannedMark = this@MarkedInfoViewModel::handleScannedMark,
                    handleScannedBox = this@MarkedInfoViewModel::handleScannedBox,
                    material = productInfo.value?.materialNumber.orEmpty()
            )
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
        return processMarkProductService.taskDescription
    }

    override fun getTaskRepo(): ITaskRepository {
        return processMarkProductService.taskRepository
    }

    override fun getProcessTotalCount(): Double {
        return processMarkProductService.getTotalCount()
    }

    override fun onClickAdd() {
        addGood()
    }

    override fun onClickApply() {
        addGood()
        processMarkProductService.apply()
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
        processMarkProductService.discard()
        return true
    }

    private fun addGood(): Boolean {
        countValue.value?.let {
            if (enabledApplyButton.value != true && it != 0.0) {
                if (getSelectedReason() === WriteOffReason.empty) {
                    screenNavigator.openNotPossibleSaveWithoutReasonScreen()
                } else {
                    screenNavigator.openNotPossibleSaveNegativeQuantityScreen()
                }
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
                funcForShoes = { _, _, originalNumber -> actionForMark(originalNumber) },
                funcForCigarettes = ::actionForMark,
                funcForCigarettesBox = ::actionForBox,
                funcForNotValidFormat = { searchGood(data) }
        )

        /*if (data.length > 60) {
            if (stampMarkedCollector.prepare(stampCode = data)) {
                markedDelegate.searchExciseStamp(data)
            } else {
                screenNavigator.openAlertDoubleScanStamp()
            }

        } else {
            if (addGood()) {
                searchProductDelegate.searchCode(data, fromScan = true)
            }
        }*/
    }

    private fun actionForMark(markNumber: String) {
        if (markedGoodStampCollector.isContainsStamp(markNumber)) {
            markSearchDelegate.requestMarkInfo(markNumber)
        } else {
            screenNavigator.openAlertDoubleScanStamp()
        }
    }

    private fun actionForBox(boxNumber: String) {
        if (markedGoodStampCollector.isContainsBox(boxNumber)) {
            markSearchDelegate.requestBoxInfo(boxNumber)
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

        /*if (isDoubleMark) {
            screenNavigator.openAlertDoubleScanStamp()
        }*/
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

}