package com.lenta.bp10.features.good_information.general

import androidx.lifecycle.MutableLiveData
import com.lenta.bp10.features.good_information.IGoodInformationRepo
import com.lenta.bp10.features.good_information.base.BaseProductInfoViewModel
import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.bp10.models.task.ProcessGeneralProductService
import com.lenta.bp10.models.task.TaskDescription
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.toStringFormatted
import javax.inject.Inject

class GoodInfoViewModel : BaseProductInfoViewModel() {

    private val processGeneralProductService: ProcessGeneralProductService by lazy {
        productInfo.value?.let {
            processServiceManager.getWriteOffTask()?.processGeneralProduct(it)
        } ?: throw IllegalAccessException("productInfo.value is not set for $this")
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
                navigator.goBack()
                limitsChecker?.check()
            }
        }

    }

    private fun addGood(): Boolean {
        countValue.value?.let {
            if (enabledApplyButton.value != true && it != DEFAULT_COUNT_VALUE) {
                showNotPossibleSaveScreen()
                return false
            }

            if (it != DEFAULT_COUNT_VALUE) {
                processGeneralProductService.add(getSelectedReason(), it)
            }

            count.value = DEFAULT_COUNT_TEXT
            requestFocusToQuantity.value = true

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
            searchProductDelegate.searchCode(code = data)
        }
    }

    override fun initCountLiveData(): MutableLiveData<String> {
        return MutableLiveData(DEFAULT_COUNT_TEXT)
    }

    companion object {
        private const val DEFAULT_COUNT_TEXT = "0"
    }

}
