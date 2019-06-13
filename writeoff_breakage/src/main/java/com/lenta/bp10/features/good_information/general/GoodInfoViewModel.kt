package com.lenta.bp10.features.good_information.general

import com.lenta.bp10.features.good_information.base.BaseProductInfoViewModel
import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.bp10.models.task.ProcessGeneralProductService
import com.lenta.bp10.models.task.TaskDescription

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

    private fun addGood(): Boolean {
        countValue.value?.let {

            if (enabledApplyButton.value != true && it != 0.0) {
                screenNavigator.openNotPossibleSaveNegativeQuantityScreen()
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



    fun onBackPressed() {
        processGeneralProductService.discard()
    }

    fun onScanResult(data: String) {
        if (addGood()) {
            searchProductDelegate.searchCode(code = data, fromScan = true)
        }
    }

}
