package com.lenta.bp10.features.good_information.excise_alco

import com.lenta.bp10.features.good_information.base.BaseProductInfoViewModel
import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.bp10.models.task.ProcessExciseAlcoProductService
import com.lenta.bp10.models.task.TaskDescription

class ExciseAlcoInfoViewModel : BaseProductInfoViewModel() {


    private val processGeneralProductService: ProcessExciseAlcoProductService by lazy {
        processServiceManager.getWriteOffTask()!!.processExciseAlcoProduct(productInfo.value!!)!!
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
                //TODO need to implement
                //processGeneralProductService.add(getSelectedReason(), it)
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
        /*if (addGood()) {
            searchProductDelegate.searchCode(code = data, fromScan = true)
        }*/
    }

}
