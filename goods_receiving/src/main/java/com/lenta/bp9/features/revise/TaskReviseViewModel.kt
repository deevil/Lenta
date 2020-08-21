package com.lenta.bp9.features.revise

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.features.task_card.TaskCardViewModel
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.model.task.revise.DocumentType
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class TaskReviseViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    val selectedPage = MutableLiveData(0)

    val taskCaption: String by lazy {
        taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    val notifications by lazy {
        MutableLiveData((taskManager.getReceivingTask()?.taskRepository?.getNotifications()?.getReviseDocumentNotifications() ?: emptyList()).mapIndexed { index, notification ->
            TaskCardViewModel.NotificationVM(number = (index + 1).toString(),
                    text = notification.text,
                    indicator = notification.indicator)
        })
    }

    val docsToCheck: MutableLiveData<List<DeliveryDocumentVM>> = MutableLiveData()
    val checkedDocs: MutableLiveData<List<DeliveryDocumentVM>> = MutableLiveData()

    private val isTaskPRCorPSP by lazy {
        val taskType = taskManager.getTaskType()
        MutableLiveData(taskType == TaskType.ReceptionDistributionCenter
                || taskType == TaskType.OwnProduction
                || taskType == TaskType.ShoppingMall
        )
    }

    val nextEnabled = docsToCheck.map { document ->
        document?.findLast { it.isObligatory } == null || isTaskPRCorPSP.value == true
    }

    val refusalVisibility by lazy {
        MutableLiveData(isTaskPRCorPSP.value == false )
    }

    val isDocsForVerification by lazy {
        taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getProductDocuments()?.isNotEmpty()
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }


    private fun updateDocumentVMs() {
        val checked = taskManager.getReceivingTask()?.getCheckedDeliveryDocuments()
        val unchecked = taskManager.getReceivingTask()?.getUncheckedDeliveryDocuments()

        checked?.let { checkedList ->
            checkedDocs.value = checkedList.mapIndexed { index, document ->
                DeliveryDocumentVM(position = checkedList.size - index,
                        name = document.documentName,
                        type = document.documentType,
                        isObligatory = document.isObligatory,
                        isCheck = true,
                        isCheckNotEnabled = document.documentType == DocumentType.Invoice || document.documentType == DocumentType.CompositeDoc,
                        isInvoice = document.documentType == DocumentType.Invoice,
                        isCompositeDoc = document.documentType == DocumentType.CompositeDoc,
                        id = document.documentID,
                        isEDO = taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getInvoiceInfo()?.isEDO == true)
            }
        }

        unchecked?.let { uncheckedList ->
            docsToCheck.value = uncheckedList.mapIndexed { index, document ->
                DeliveryDocumentVM(position = uncheckedList.size - index,
                        name = document.documentName,
                        type = document.documentType,
                        isObligatory = document.isObligatory,
                        isCheck = false,
                        isCheckNotEnabled = document.documentType == DocumentType.Invoice || document.documentType == DocumentType.CompositeDoc,
                        isInvoice = document.documentType == DocumentType.Invoice,
                        isCompositeDoc = document.documentType == DocumentType.CompositeDoc,
                        id = document.documentID,
                        isEDO = taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getInvoiceInfo()?.isEDO == true)
            }
        }

        launchUITryCatch {
            moveToPreviousPageIfNeeded()
        }
    }

    fun checkedChanged(position: Int, checked: Boolean) {
        val docID = if (checked) docsToCheck.value?.get(position)?.id else checkedDocs.value?.get(position)?.id
        docID?.let {
            taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.changeDeliveryDocumentStatus(it)
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

    private fun onClickOnDocument(document: DeliveryDocumentVM) {
        if (document.isInvoice) {
            screenNavigator.openInvoiceReviseScreen()
        }
        if (document.isCompositeDoc) {
            taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getDeliveryDocuments()?.findLast {
                it.documentID == document.id
            }?.let {
                screenNavigator.openCompositeDocReviseScreen(it)
            }
        }
    }

    fun onResume() {
        updateDocumentVMs()
    }

    fun onClickRefusal() {
        screenNavigator.openRejectScreen()
    }

    fun onClickSave() {
        val isObligatoryDocs: Boolean = docsToCheck.value?.findLast {
            it.isObligatory
        }?.isObligatory ?: false

        if (isTaskPRCorPSP.value == true && isDocsForVerification == false && isObligatoryDocs) {
            screenNavigator.openRemainsUnconfirmedBindingDocsPRCDialog(
                    nextCallbackFunc = {
                        screenNavigator.openFinishReviseLoadingScreen()
                    }
            )
            return
        }

        if (isDocsForVerification == true) {
            screenNavigator.openProductDocumentsReviseScreen()
        } else {
            screenNavigator.openFinishReviseLoadingScreen()
        }
    }

    fun onBackPressed() {
        screenNavigator.openUnlockTaskLoadingScreen()
    }
}

data class DeliveryDocumentVM(
        val position: Int,
        val name: String,
        val type: DocumentType,
        val isObligatory: Boolean,
        val isCheck: Boolean,
        val isCheckNotEnabled: Boolean,
        val isInvoice: Boolean,
        val isCompositeDoc: Boolean,
        val id: String,
        val isEDO: Boolean
)