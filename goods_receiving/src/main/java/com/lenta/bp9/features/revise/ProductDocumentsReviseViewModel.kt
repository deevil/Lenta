package com.lenta.bp9.features.revise

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.features.task_card.TaskCardViewModel
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.model.task.revise.ProductBatchRevise
import com.lenta.bp9.model.task.revise.ProductDocumentType
import com.lenta.bp9.model.task.revise.ProductVetDocumentRevise
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.orIfNull
import com.mobrun.plugin.api.HyperHive
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

    val typeTask: TaskType by lazy {
        taskManager.getTaskType()
    }

    val taskCaption: String by lazy {
        taskManager.getReceivingTask()?.taskHeader?.caption.orEmpty()
    }

    val notifications by lazy {
        MutableLiveData((taskManager.getReceivingTask()?.taskRepository?.getNotifications()?.getReviseProductNotifications()
                ?: emptyList()).mapIndexed { index, notification ->
            TaskCardViewModel.NotificationVM(number = (index + 1).toString(),
                    text = notification.text,
                    indicator = notification.indicator)
        })
    }

    val sortEnabled = selectedPage.map { it != 2 }

    val docsToCheck: MutableLiveData<List<ProductDocumentVM>> = MutableLiveData()
    val checkedDocs: MutableLiveData<List<ProductDocumentVM>> = MutableLiveData()

    var currentSortMode: SortMode = SortMode.ProductNumber

    private val isTaskPRCorPSP by lazy {
        val taskType = taskManager.getTaskType()
        MutableLiveData(taskType == TaskType.ReceptionDistributionCenter
                || taskType == TaskType.OwnProduction
                || taskType == TaskType.ShoppingMall
        )
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onResume() {
        updateDocumentVMs()
    }

    private fun updateDocumentVMs() {
        //2.2. Устанавливать чек-бокс "Сверено" по веттовару, если в таблице ET_VET_CHK для текущего товара есть записи и для всех записей установлен признак FLG_CHECK
        // (Т.е. сверенным считается товар у которого все привязанные ВСД сверены, суммарные количества в привязанных ВСД проверять на этом этапе не нужно)
        //карточка 2460
        taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.setProductVetDocumentsReconciliation()

        val checked = taskManager.getReceivingTask()?.getCheckedProductDocuments()
        val unchecked = taskManager.getReceivingTask()?.getUncheckedProductDocuments()

        checked?.let { checkedList ->
            checkedDocs.value = checkedList.sortedBy { if (currentSortMode == SortMode.DocumentName) it.documentName else it.productNumber }.mapIndexed { index, document ->
                ProductDocumentVM(position = checkedList.size - index,
                        sortName1 = if (currentSortMode == SortMode.DocumentName) document.documentName else document.productNumber.takeLast(6) + " " + (ZfmpUtz48V001.getProductInfoByMaterial(document.productNumber)?.name ?: ""),
                        sortName2 = if (currentSortMode == SortMode.ProductNumber) document.documentName else document.productNumber.takeLast(6) + " " + (ZfmpUtz48V001.getProductInfoByMaterial(document.productNumber)?.name ?: ""),
                        productName = document.productNumber.takeLast(6) + " " + (ZfmpUtz48V001.getProductInfoByMaterial(document.productNumber)?.name
                                ?: ""),
                        type = document.documentType,
                        isObligatory = document.isObligatory,
                        isCheck = true,
                        isVisibileArrow = document.documentType == ProductDocumentType.AlcoImport || document.documentType == ProductDocumentType.AlcoRus || document.documentType == ProductDocumentType.Mercury,
                        id = document.documentID,
                        matnr = document.productNumber,
                        isSet = document.isSet)
            }
        }

        unchecked?.let { uncheckedList ->
            docsToCheck.value = uncheckedList.sortedBy { if (currentSortMode == SortMode.DocumentName) it.documentName else it.productNumber }.mapIndexed { index, document ->
                ProductDocumentVM(position = uncheckedList.size - index,
                        sortName1 = if (currentSortMode == SortMode.DocumentName) document.documentName else document.productNumber.takeLast(6) + " " + (ZfmpUtz48V001.getProductInfoByMaterial(document.productNumber)?.name ?: ""),
                        sortName2 = if (currentSortMode == SortMode.ProductNumber) document.documentName else document.productNumber.takeLast(6) + " " + (ZfmpUtz48V001.getProductInfoByMaterial(document.productNumber)?.name ?: ""),
                        productName = document.productNumber.takeLast(6) + " " + (ZfmpUtz48V001.getProductInfoByMaterial(document.productNumber)?.name ?: ""),
                        type = document.documentType,
                        isObligatory = document.isObligatory,
                        isCheck = false,
                        isVisibileArrow = document.documentType == ProductDocumentType.AlcoImport || document.documentType == ProductDocumentType.AlcoRus || document.documentType == ProductDocumentType.Mercury,
                        id = document.documentID,
                        matnr = document.productNumber,
                        isSet = document.isSet)
            }
        }

        launchUITryCatch {
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
        if (document.isVisibileArrow) {
            val batches = taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getProductBatches()?.filter { it.productNumber == document.matnr }
            if (!batches.isNullOrEmpty()) {
                actionWhenBatchesIsNotEmpty(batches, document)
            } else {
                actionWhenBatchesIsEmpty(document)
            }
        }
    }

    private fun actionWhenBatchesIsEmpty(document: ProductDocumentVM) {
        when (document.type) {
            ProductDocumentType.Mercury -> {
                actionsWhenProductDocumentIsMercury(document)
            }
            else -> Unit
        }
    }

    private fun actionsWhenProductDocumentIsMercury(document: ProductDocumentVM) {
        val products = taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getProductVetDocuments()?.filter {
            it.productNumber == document.matnr
        }
        if (products.isNullOrEmpty()) {
            screenNavigator.openAlertVADProductNotMatchedScreen(document.productName)
        } else {
            taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getProductDocuments()?.findLast { dpdr ->
                dpdr.documentID == document.id
            }?.let { productDoc ->
                screenNavigator.openMercuryListScreen(productDoc)
            }.orIfNull {
                Logg.e { "productDoc is null or not finded" }
                screenNavigator.openAlertProductDocumentsNotFoundScreen()
            }
        }
    }

    private fun actionWhenBatchesIsNotEmpty(batches: List<ProductBatchRevise>, document: ProductDocumentVM) {
        if (batches.size > 1) {
            screenNavigator.openAlcoholBatchSelectScreen(document.matnr, document.type)
        } else {
            actionsForDocumentType(batches, document)
        }
    }

    private fun actionsForDocumentType(batches: List<ProductBatchRevise>, document: ProductDocumentVM) {
        when (document.type) {
            ProductDocumentType.AlcoImport -> {
                screenNavigator.openImportAlcoFormReviseScreen(batches.first().productNumber, batches.first().batchNumber)
            }
            ProductDocumentType.AlcoRus -> {
                screenNavigator.openRussianAlcoFormReviseScreen(batches.first().productNumber, batches.first().batchNumber)
            }
            else -> Unit
        }
    }

    fun onClickReject() {
        screenNavigator.openRejectScreen()
    }

    fun onClickSort() {
        currentSortMode = if (currentSortMode == SortMode.DocumentName) {
            SortMode.ProductNumber
        } else {
            SortMode.DocumentName
        }
        updateDocumentVMs()
    }

    fun onClickSave() {
        if (isTaskPRCorPSP.value == true && !taskManager.getReceivingTask()?.getObligatoryDeliveryDocuments().isNullOrEmpty()) {
            screenNavigator.openRemainsUnconfirmedBindingDocsPRCDialog(
                    nextCallbackFunc = {
                        saveData()
                    }
            )
            return
        }

        if (docsToCheck.value?.findLast { it.isObligatory && it.type != ProductDocumentType.Mercury} != null) { //https://trello.com/c/mVRK8Uok проверяем все документы, кроме меркурия, т.к. по меркурию будет позже проверено в saveData()
            screenNavigator.openConfirmationProcessAsDiscrepancy {
                saveData()
            }
        } else {
            saveData()
        }
    }

    private fun saveData() {
        val presenceMercury = taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getProductDocuments()?.any {
            it.documentType == ProductDocumentType.Mercury
        }

        if (presenceMercury == true) {
            if (taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.presenceUncoveredVadAllGoods() == true) {
                screenNavigator.openDiscrepanciesNoVerifiedVadDialog(
                        {screenNavigator.openMercuryExceptionIntegrationScreen()},
                        {screenNavigator.openFinishReviseLoadingScreen()}
                )
                return
            }
            if (taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.presenceUncoveredVadSomeGoods() == true) {
                screenNavigator.openDiscrepanciesInconsistencyVetDocsDialog{
                    screenNavigator.openFinishReviseLoadingScreen()
                }
                return
            }
        }

        screenNavigator.openFinishReviseLoadingScreen()
    }
}

data class ProductDocumentVM(
        val position: Int,
        val sortName1: String,
        val sortName2: String,
        val productName: String,
        val type: ProductDocumentType,
        val isObligatory: Boolean,
        val isCheck: Boolean,
        val isSet: Boolean,
        val isVisibileArrow: Boolean,
        val id: String,
        val matnr: String
)

enum class SortMode {
    DocumentName,
    ProductNumber
}