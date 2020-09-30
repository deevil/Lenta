package com.lenta.bp10.features.good_information.excise_alco

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lenta.bp10.features.good_information.base.BaseProductInfoViewModel
import com.lenta.bp10.models.AlcoholStampCollector
import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.bp10.models.task.ProcessExciseAlcoProductService
import com.lenta.bp10.models.task.TaskDescription
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import javax.inject.Inject

class ExciseAlcoInfoViewModel : BaseProductInfoViewModel() {

    @Inject
    lateinit var exciseAlcoDelegate: ExciseAlcoDelegate

    val rollBackEnabled: LiveData<Boolean> by lazy {
        countValue.map { it ?: 0.0 > 0.0 }
    }

    private val processExciseAlcoProductService: ProcessExciseAlcoProductService by lazy {
        processServiceManager.getWriteOffTask()!!.processExciseAlcoProduct(productInfo.value!!)!!
    }

    private val alcoholStampCollector: AlcoholStampCollector by lazy {
        AlcoholStampCollector(processExciseAlcoProductService)
    }

    init {
        launchUITryCatch {
            exciseAlcoDelegate.init(
                    handleNewStamp = this@ExciseAlcoInfoViewModel::handleNewStamp,
                    tkNumber = getTaskDescription().tkNumber,
                    materialNumber = productInfo.value!!.materialNumber
            )
        }
    }

    override fun getProcessTotalCount(): Double {
        return processExciseAlcoProductService.getTotalCount()
    }

    override fun getTaskRepo(): ITaskRepository {
        return processExciseAlcoProductService.taskRepository
    }

    override fun getTaskDescription(): TaskDescription {
        return processExciseAlcoProductService.taskDescription
    }

    override fun onClickAdd() {
        addGood()
    }


    override fun onClickApply() {
        addGood()
        processExciseAlcoProductService.apply()
        navigator.goBack()
    }


    override fun handleFragmentResult(code: Int?): Boolean {
        if (exciseAlcoDelegate.handleResult(code)) {
            return true
        }
        return super.handleFragmentResult(code)
    }

    private fun addGood(): Boolean {
        countValue.value?.let {
            if (enabledApplyButton.value != true && it != 0.0) {
                showNotPossibleSaveScreen()
                return false
            }

            if (it != 0.0) {
                alcoholStampCollector.processAll(getSelectedReason())
            }

            count.value = "0"
            requestFocusToQuantity.value = true

            return true
        }

        return false
    }

    private fun handleNewStamp(isBadStamp: Boolean) {
        if (!alcoholStampCollector.add(
                        materialNumber = productInfo.value!!.materialNumber,
                        setMaterialNumber = "",
                        writeOffReason = getSelectedReason().code,
                        isBadStamp = isBadStamp
                )) {
            navigator.openAlertDoubleScanStamp()
        }
    }


    override fun onBackPressed(): Boolean {
        if (alcoholStampCollector.isNotEmpty()) {
            navigator.openConfirmationToBackNotEmptyStampsScreen {
                navigator.goBack()
            }
            return false
        }
        processExciseAlcoProductService.discard()
        return true
    }

    override fun onScanResult(data: String) {
        if (data.length > 60) {
            if (alcoholStampCollector.prepare(stampCode = data)) {
                exciseAlcoDelegate.searchExciseStamp(data)
            } else {
                navigator.openAlertDoubleScanStamp()
            }

        } else {
            if (addGood()) {
                searchProductDelegate.searchCode(data)
            }
        }
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

    override fun initCountLiveData(): MutableLiveData<String> {
        return alcoholStampCollector.observeCount().map { it.toStringFormatted() }
    }

    fun onClickRollBack() {
        alcoholStampCollector.rollback()
    }

    fun onClickDamaged() {
        alcoholStampCollector.addBadMark(
                material = productInfo.value?.materialNumber.orEmpty(),
                writeOffReason = getSelectedReason().code
        )
    }

}
