package com.lenta.bp10.features.good_information.excise_alco

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.features.good_information.base.BaseProductInfoViewModel
import com.lenta.bp10.models.StampCollector
import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.bp10.models.task.ProcessExciseAlcoProductService
import com.lenta.bp10.models.task.TaskDescription
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import kotlinx.coroutines.launch
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

    private val stampCollector: StampCollector by lazy {
        StampCollector(processExciseAlcoProductService)
    }

    init {
        viewModelScope.launch {
            exciseAlcoDelegate.init(
                    viewModelScope = this@ExciseAlcoInfoViewModel::viewModelScope,
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
        screenNavigator.goBack()
    }


    override fun onResult(code: Int?) {
        if (exciseAlcoDelegate.handleResult(code)) {
            return
        }
        super.onResult(code)
    }

    private fun addGood(): Boolean {
        countValue.value?.let {

            if (enabledApplyButton.value != true && it != 0.0) {
                screenNavigator.openNotPossibleSaveNegativeQuantityScreen()
                return false
            }

            if (it != 0.0) {
                stampCollector.processAll(getSelectedReason())
            }

            count.value = ""

            return true
        }
        return false
    }

    private fun handleNewStamp(isBadStamp: Boolean) {
        if (!stampCollector.add(
                        materialNumber = productInfo.value!!.materialNumber,
                        setMaterialNumber = "",
                        writeOffReason = getSelectedReason().code,
                        isBadStamp = isBadStamp
                )) {
            screenNavigator.openAlertDoubleScanStamp()
        }
    }


    override fun onBackPressed() {
        processExciseAlcoProductService.discard()
    }

    override fun onScanResult(data: String) {
        if (data.length > 60) {
            if (stampCollector.prepare(stampCode = data)) {
                exciseAlcoDelegate.searchExciseStamp(data)
            } else {
                screenNavigator.openAlertDoubleScanStamp()
            }

        } else {
            if (addGood()) {
                searchProductDelegate.searchCode(data, fromScan = true)
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
        return stampCollector.observeCount().map { it.toStringFormatted() }
    }

    fun onClickRollBack() {
        stampCollector.rollback()
    }

}
