package com.lenta.bp9.features.revise

import android.content.Context
import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.task_card.TaskCardViewModel
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.revise.ProductDocumentType
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMatcode
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.map
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProductDocumentsReviseViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var hyperHive: HyperHive

    private val ZfmpUtz48V001: ZfmpUtz48V001 by lazy {
        ZfmpUtz48V001(hyperHive)
    }

    val selectedPage = MutableLiveData(0)

    val taskCaption: String by lazy {
        taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    val notifications by lazy {
        MutableLiveData((taskManager.getReceivingTask()?.taskRepository?.getNotifications()?.getReviseProductNotifications() ?: emptyList()).mapIndexed { index, notification ->
            TaskCardViewModel.NotificationVM(number = (index + 1).toString(),
                    text = notification.text,
                    indicator = notification.indicator)
        })
    }

    val sortEnabled = selectedPage.map { it != 2 }

    val docsToCheck: MutableLiveData<List<ProductDocumentVM>> = MutableLiveData()
    val checkedDocs: MutableLiveData<List<ProductDocumentVM>> = MutableLiveData()

    var currentSortMode: SortMode = SortMode.DocumentName

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onResume() {
        updateDocumentVMs()
    }

    private fun updateDocumentVMs() {
        val checked = taskManager.getReceivingTask()?.getCheckedProductDocuments()
        val unchecked = taskManager.getReceivingTask()?.getUncheckedProductDocuments()

        checked?.let { checkedList ->
            checkedDocs.value = checkedList.sortedBy { if (currentSortMode == SortMode.DocumentName) it.documentName else it.productNumber }.mapIndexed { index, document ->
                ProductDocumentVM(position = checkedList.size - index,
                        name = document.documentName,
                        productName = document.productNumber.takeLast(6) + " " + (ZfmpUtz48V001.getProductInfoByMaterial(document.productNumber)?.name ?: ""),
                        type = document.documentType,
                        isObligatory = document.isObligatory,
                        isCheck = true,
                        isABForm = document.documentType == ProductDocumentType.AlcoImport || document.documentType == ProductDocumentType.AlcoRus,
                        id = document.documentID,
                        matnr = document.productNumber,
                        isSet = document.isSet)
            }
        }

        unchecked?.let { uncheckedList ->
            docsToCheck.value = uncheckedList.sortedBy { if (currentSortMode == SortMode.DocumentName) it.documentName else it.productNumber }.mapIndexed { index, document ->
                ProductDocumentVM(position = uncheckedList.size - index,
                        name = document.documentName,
                        productName = document.productNumber.takeLast(6) + " " + (ZfmpUtz48V001.getProductInfoByMaterial(document.productNumber)?.name ?: ""),
                        type = document.documentType,
                        isObligatory = document.isObligatory,
                        isCheck = false,
                        isABForm = document.documentType == ProductDocumentType.AlcoImport || document.documentType == ProductDocumentType.AlcoRus,
                        id = document.documentID,
                        matnr = document.productNumber,
                        isSet = document.isSet)
            }
        }

        viewModelScope.launch {
            moveToPreviousPageIfNeeded()
        }
    }

    fun checkedChanged(position: Int, checked: Boolean) {
        val doc = if (checked) docsToCheck.value?.get(position) else checkedDocs.value?.get(position)
        doc?.let {
            taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.changeProductDocumentStatus(it.id, it.matnr)
            updateDocumentVMs()
        }
    }

    private fun moveToPreviousPageIfNeeded() {
        if (selectedPage.value == 0) {
            selectedPage.value = if (docsToCheck.value?.size == 0 && checkedDocs.value?.size != 0) 1 else 0
        } else {
            selectedPage.value = if (checkedDocs.value?.size == 0) 0 else 1
        }
    }

    fun onClickCheckedPosition(position: Int) {
        checkedDocs.value?.get(position)?.let {
            onClickOnDocument(it)
        }
    }

    fun onClickUncheckedPosition(position: Int) {
        docsToCheck.value?.get(position)?.let {
            onClickOnDocument(it)
        }
    }

    private fun onClickOnDocument(document: ProductDocumentVM) {
        if (document.isABForm) {
            val batches = taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getProductBatches()?.filter { it.productNumber == document.matnr }
            if (!batches.isNullOrEmpty()) {
                if (batches.size > 1) {
                    screenNavigator.openAlcoholBatchSelectScreen(document.matnr, document.type)
                } else {
                    when (document.type) {
                        ProductDocumentType.AlcoImport -> {
                            screenNavigator.openImportAlcoFormReviseScreen(batches.first().productNumber, batches.first().batchNumber)
                        }
                        ProductDocumentType.AlcoRus -> {
                            screenNavigator.openRussianAlcoFormReviseScreen(batches.first().productNumber, batches.first().batchNumber)
                        }
                    }
                }
            }
        }
    }

    fun onClickReject() {
        screenNavigator.openRejectScreen()
    }

    fun onClickSort() {
        if (currentSortMode == SortMode.DocumentName) {
            currentSortMode = SortMode.ProductNumber
        } else {
            currentSortMode = SortMode.DocumentName
        }
        updateDocumentVMs()
    }

    fun onClickSave() {
        if (docsToCheck.value?.findLast { it.isObligatory } != null) {
            screenNavigator.openConfirmationProcessAsDiscrepancy {
                saveData()
            }
        } else {
            saveData()
        }
    }

    private fun saveData() {
        screenNavigator.openFinishReviseLoadingScreen()
    }
}

data class ProductDocumentVM(
        val position: Int,
        val name: String,
        val productName: String,
        val type: ProductDocumentType,
        val isObligatory: Boolean,
        val isCheck: Boolean,
        val isSet: Boolean,
        val isABForm: Boolean,
        val id: String,
        val matnr: String
)

enum class SortMode{
    DocumentName,
    ProductNumber
}