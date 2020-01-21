package com.lenta.bp9.features.mercury_list

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.revise.DeliveryProductDocumentRevise
import com.lenta.bp9.model.task.revise.ProductVetDocumentRevise
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import kotlinx.coroutines.launch
import javax.inject.Inject

class MercuryListViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    val productDoc: MutableLiveData<DeliveryProductDocumentRevise> = MutableLiveData()
    val selectedPage = MutableLiveData(0)
    val tiedSelectionsHelper = SelectionItemsHelper()
    val untiedSelectionsHelper = SelectionItemsHelper()
    val listTied: MutableLiveData<List<MercuryListItem>> = MutableLiveData()
    val listUntied: MutableLiveData<List<MercuryListItem>> = MutableLiveData()
    private val origProductVetDocuments: MutableLiveData<List<ProductVetDocumentRevise>?> = MutableLiveData()

    val tiedEnabled: MutableLiveData<Boolean> = untiedSelectionsHelper.selectedPositions.map {
        val selectedComponentsPositions = untiedSelectionsHelper.selectedPositions.value
        !selectedComponentsPositions.isNullOrEmpty()
    }

    val untiedEnabled: MutableLiveData<Boolean> = tiedSelectionsHelper.selectedPositions.map {
        val selectedComponentsPositions = tiedSelectionsHelper.selectedPositions.value
        !selectedComponentsPositions.isNullOrEmpty()
    }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    init {
        viewModelScope.launch {
            origProductVetDocuments.value = taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getProductVetDocuments()?.map {
                it.copy()
            }
        }
    }

    fun onResume() {
        updateListTied()
        updateListUntied()
    }

    private fun updateListTied() {
        taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getProductVetDocuments().let {lpvdr ->
            listTied.postValue(
                    lpvdr?.filter {
                        it.isAttached
                    }?.mapIndexed { index, productVetDocumentRevise ->
                        MercuryListItem(
                                number = index + 1,
                                name = productVetDocumentRevise.productName,
                                quantityWithUom = "${productVetDocumentRevise.volume.toStringFormatted()} ${productVetDocumentRevise.measureUnits.name}",
                                isCheck = productVetDocumentRevise.isCheck,
                                productVetDocument = productVetDocumentRevise,
                                even = index % 2 == 0)
                    }?.reversed()
            )
        }
        tiedSelectionsHelper.clearPositions()
    }

    private fun updateListUntied() {
        taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getProductVetDocuments().let {lpvdr ->
            listUntied.postValue(
                    lpvdr?.filter {
                        !it.isAttached
                    }?.mapIndexed { index, productVetDocumentRevise ->
                        MercuryListItem(
                                number = index + 1,
                                name = productVetDocumentRevise.productName,
                                quantityWithUom = "${productVetDocumentRevise.volume.toStringFormatted()} ${productVetDocumentRevise.measureUnits.name}",
                                isCheck = productVetDocumentRevise.isCheck,
                                productVetDocument = productVetDocumentRevise,
                                even = index % 2 == 0)
                    }?.reversed()
            )
        }
        untiedSelectionsHelper.clearPositions()
    }


    fun onClickItemPosition(position: Int) {
        val productVetDocument: ProductVetDocumentRevise = if (selectedPage.value == 0) {
            listTied.value!![position].productVetDocument
        } else {
            listUntied.value!![position].productVetDocument
        }
        screenNavigator.openReconciliationMercuryScreen(productVetDocument)
    }

    fun onClickTiedUntied() {
        if (selectedPage.value == 0) {
            tiedSelectionsHelper.selectedPositions.value?.map { position ->
                if (listTied.value!![position].productVetDocument.attachType != "3") {
                    taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.changeProductVetDocumentStatus(listTied.value!![position].productVetDocument, false)
                }
            }
        } else {
            untiedSelectionsHelper.selectedPositions.value?.map { position ->
                if (listUntied.value!![position].productVetDocument.attachType != "3") {
                    taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.changeProductVetDocumentStatus(listUntied.value!![position].productVetDocument, true)
                }
            }
        }

        updateListTied()
        updateListUntied()
    }

    fun onClickNext() {
        screenNavigator.goBack()
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onBackPressed() {
        val newProductVetDocuments = taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getProductVetDocuments()
        val isDiscrepancies = origProductVetDocuments.value?.filter {origProdVetDoc ->
            val newProdVetDoc = newProductVetDocuments?.findLast {
                it.vetDocumentID == origProdVetDoc.vetDocumentID && it.productNumber == origProdVetDoc.productNumber
            }
            origProdVetDoc.isCheck == newProdVetDoc?.isCheck
        }?.size != origProductVetDocuments.value?.size

        if ( isDiscrepancies) {
            screenNavigator.openUnsavedDataDialog(
                    yesCallbackFunc = {
                        origProductVetDocuments.value?.map {
                            taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.changeProductVetDocumentStatus(it, it.isCheck)
                        }
                        screenNavigator.goBack()
                    }
            )
        } else {
            screenNavigator.goBack()
        }
    }

}
