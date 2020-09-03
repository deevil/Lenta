package com.lenta.bp10.features.good_information.general

import androidx.lifecycle.MutableLiveData
import com.lenta.bp10.features.good_information.base.BaseProductInfoViewModel
import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.bp10.models.task.ProcessGeneralProductService
import com.lenta.bp10.models.task.TaskDescription
import com.lenta.bp10.models.task.WriteOffReason
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.toStringFormatted

class GoodInfoViewModel : BaseProductInfoViewModel() {


    private val processGeneralProductService: ProcessGeneralProductService by lazy {
        processServiceManager.getWriteOffTask()!!.processGeneralProduct(productInfo.value!!)!!
    }

    override fun getProcessTotalCount(): Double {
        return processGeneralProductService.getTotalCount()
    }

    override fun getTaskRepo(): ITaskRepository {
        return processGeneralProductService.taskRepository
    }

    override fun handleProductSearchResult(scanInfoResult: ScanInfoResult?): Boolean {
        scanInfoResult?.let {
            if (it.productInfo.materialNumber == productInfo.value?.materialNumber) {
                count.value = it.quantity.toStringFormatted()
                return true
            }
        }
        onClickApply()
        return false
    }

    override fun getTaskDescription(): TaskDescription {
        return processGeneralProductService.taskDescription
    }

    override fun onClickAdd() {
        if (addGood()) {
            launchUITryCatch {
                limitsChecker?.check()
            }
        }
    }


    override fun onClickApply() {
        addGood().let { wasAdded ->
            if (wasAdded) {
                processGeneralProductService.apply()
                screenNavigator.goBack()
                limitsChecker?.check()
            }
        }

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
                processGeneralProductService.add(getSelectedReason(), it)
            }

            count.value = ""

            return true
        }
        return false
    }


    override fun onBackPressed(): Boolean {
        processGeneralProductService.discard()
        return true
    }

    override fun onScanResult(data: String) {
        if (addGood()) {
            searchProductDelegate.searchCode(code = data, fromScan = true)
        }
    }

    override fun initCountLiveData(): MutableLiveData<String> {
        return MutableLiveData("0")
    }

}
