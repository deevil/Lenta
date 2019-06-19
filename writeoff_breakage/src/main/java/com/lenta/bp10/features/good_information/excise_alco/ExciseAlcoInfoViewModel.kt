package com.lenta.bp10.features.good_information.excise_alco

import androidx.lifecycle.viewModelScope
import com.lenta.bp10.features.good_information.base.BaseProductInfoViewModel
import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.bp10.models.task.ProcessExciseAlcoProductService
import com.lenta.bp10.models.task.TaskDescription
import com.lenta.bp10.requests.network.ExciseStampNetRequest
import com.lenta.bp10.requests.network.ExciseStampParams
import com.lenta.bp10.requests.network.ExciseStampRestInfo
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExciseAlcoInfoViewModel : BaseProductInfoViewModel() {

    @Inject
    lateinit var exciseStampNetRequest: ExciseStampNetRequest

    val rollBackEnabled = countValue.map { it ?: 0.0 > 0.0 }


    private val processGeneralProductService: ProcessExciseAlcoProductService by lazy {
        processServiceManager.getWriteOffTask()!!.processExciseAlcoProduct(productInfo.value!!)!!
    }

    private val stampCollector: StampCollector by lazy {
        StampCollector(processGeneralProductService, count)
    }

    override fun getProcessTotalCount(): Double {
        return processGeneralProductService.getTotalCount()
    }

    override fun getTaskRepo(): ITaskRepository {
        return processGeneralProductService.taskRepository
    }

    override fun getTaskDescription(): TaskDescription {
        return processGeneralProductService.taskDescription
    }

    override fun onClickAdd() {
        addGood()
    }


    override fun onClickApply() {
        addGood()
        processGeneralProductService.apply()
        screenNavigator.goBack()
    }

    private fun searchExciseStamp(code: String) {
        viewModelScope.launch {
            screenNavigator.showProgress(exciseStampNetRequest)

            exciseStampNetRequest(ExciseStampParams(
                    pdf417 = code,
                    werks = getTaskDescription().tkNumber,
                    matnr = productInfo.value!!.materialNumber))
                    .either(::handleFailure, ::handleExciseStampSuccess)

            screenNavigator.hideProgress()
        }
    }

    private fun handleExciseStampSuccess(exciseStampRestInfo: List<ExciseStampRestInfo>) {

        val retCode = exciseStampRestInfo[1].data[0][0].toInt()
        val serverDescription = exciseStampRestInfo[1].data[0][1]

        when (retCode) {
            0 -> {
                if (!stampCollector.add(
                                materialNumber = productInfo.value!!.materialNumber,
                                setMaterialNumber = "",
                                writeOffReason = getSelectedReason().code,
                                isBasStamp = false
                        )) {
                    screenNavigator.openAlertDoubleScanStamp()
                }
            }
            else -> screenNavigator.openInfoScreen(serverDescription)
        }
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


    override fun onBackPressed() {
        processGeneralProductService.discard()
    }

    override fun onScanResult(data: String) {
        if (data.length > 18) {
            if (stampCollector.prepare(stampCode = data)) {
                searchExciseStamp(data)
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

    fun onClickRollBack() {
        stampCollector.rollback()
    }

}
