package com.lenta.bp9.features.reconciliation_mercury

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.revise.ProductVetDocumentRevise
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class ReconciliationMercuryViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    val productVetDoc: MutableLiveData<ProductVetDocumentRevise> = MutableLiveData()
    private val origProductVetDoc: MutableLiveData<ProductVetDocumentRevise> = MutableLiveData()
    val sapNameGoods: MutableLiveData<String> = productVetDoc.map { it?.originProductName}
    val mercuryNameGoods: MutableLiveData<String> = productVetDoc.map { it?.productName}
    val ligamentType: MutableLiveData<String> = productVetDoc.map { it?.attachText}
    val reconciliationCheck: MutableLiveData<Boolean> = productVetDoc.map { it?.isCheck}
    val enabledReconciliationCheck: MutableLiveData<Boolean> = productVetDoc.map { it?.attachType != "3"}

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    init {
        viewModelScope.launch {
            origProductVetDoc.value = productVetDoc.value?.copy()
        }
    }

    fun onClickReconciliationCheck(isChecked: Boolean) {
        taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.changeProductVetDocumentReconciliation(productVetDoc.value!!, isChecked)
    }

    fun onClickTiedUntied() {
        if (productVetDoc.value!!.isAttached) {
            taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.changeProductVetDocumentStatus(productVetDoc.value!!, false)
        } else {
            taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.changeProductVetDocumentStatus(productVetDoc.value!!, true)
        }
        screenNavigator.goBack()
    }

    fun onClickNext() {
        screenNavigator.goBack()
    }

    fun onBackPressed() {
        val newProductVetDocument = taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getProductVetDocuments()?.findLast {
            it.vetDocumentID == productVetDoc.value?.vetDocumentID && it.productNumber == productVetDoc.value?.productNumber
        }
        if ( origProductVetDoc.value?.isCheck != newProductVetDocument?.isCheck) {
            screenNavigator.openUnsavedDataDialog(
                    yesCallbackFunc = {
                        taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.changeProductVetDocumentStatus(origProductVetDoc.value!!, origProductVetDoc.value!!.isAttached)
                        taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.changeProductVetDocumentReconciliation(productVetDoc.value!!, origProductVetDoc.value!!.isCheck)
                        screenNavigator.goBack()
                    }
            )
        } else {
            screenNavigator.goBack()
        }
    }
}
