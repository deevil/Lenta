package com.lenta.bp9.features.revise

import android.content.Context
import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.task_card.TaskCardViewModel
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.revise.DocumentType
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
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

    val nextEnabled = docsToCheck.map { document -> document?.findLast { it.isObligatory } == null }

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
                        isInvoice = document.documentType == DocumentType.Invoice,
                        id = document.documentID)
            }
        }

        unchecked?.let { uncheckedList ->
            docsToCheck.value = uncheckedList.mapIndexed { index, document ->
                DeliveryDocumentVM(position = uncheckedList.size - index,
                        name = document.documentName,
                        type = document.documentType,
                        isObligatory = document.isObligatory,
                        isCheck = false,
                        isInvoice = document.documentType == DocumentType.Invoice,
                        id = document.documentID)
            }
        }

        viewModelScope.launch {
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
    }

    fun onResume() {
        updateDocumentVMs()
    }

    fun onClickReject() {
        Logg.d { "Reject screen" }
    }

    fun onClickNext() {
        Logg.d { "Product documents screen" }
    }


    // TODO: Implement the ViewModel
}

data class DeliveryDocumentVM(
        val position: Int,
        val name: String,
        val type: DocumentType,
        val isObligatory: Boolean,
        val isCheck: Boolean,
        val isInvoice: Boolean,
        val id: String
)