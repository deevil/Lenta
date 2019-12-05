package com.lenta.bp9.features.loading.tasks

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.model.task.*
import com.lenta.bp9.model.task.revise.*
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.network.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoadingTaskCardViewModel : CoreLoadingViewModel() {
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskCardNetRequest: TaskCardNetRequest
    @Inject
    lateinit var taskContentsNetRequest: TaskContentsNetRequest
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder
    @Inject
    lateinit var taskContents: TaskContents
    @Inject
    lateinit var hyperHive: HyperHive

    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    var mode: TaskCardMode = TaskCardMode.None
    var taskNumber: String = ""
    var loadFullData: Boolean = false

    init {
        viewModelScope.launch {
            progress.value = true
            val params = TaskContentRequestParameters(mode = mode.TaskCardModeString,
                    deviceIP = context.getDeviceIp(),
                    personalNumber = sessionInfo.personnelNumber ?: "",
                    taskNumber = taskNumber
            )
            if (loadFullData) {
                taskContentsNetRequest(params).either(::handleFailure, ::handleFullDataSuccess)
            } else {
                taskCardNetRequest(params).either(::handleFailure, ::handleSuccess)
            }
            progress.value = false
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.goBack()
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleSuccess(result: TaskCardRequestResult) {
        Logg.d { "Task card request result $result" }
        screenNavigator.goBack()
        val taskHeader = repoInMemoryHolder.taskList.value?.tasks?.findLast { it.taskNumber == taskNumber }
        taskHeader?.let {
            val notifications = result.notifications.map { TaskNotification.from(it) }
            val newTask = taskManager.newReceivingTask(taskHeader, TaskDescription.from(result.taskDescription))
            newTask?.taskRepository?.getNotifications()?.updateWithNotifications(notifications, null, null, null)
            taskManager.setTask(newTask)
            screenNavigator.openTaskCardScreen(mode)
        }
    }

    private fun handleFullDataSuccess(result: TaskContentsRequestResult) {
        Logg.d { "Task card request result $result" }
        //screenNavigator.goBack()
        viewModelScope.launch {
            val taskHeader = repoInMemoryHolder.taskList.value?.tasks?.findLast { it.taskNumber == taskNumber }
            taskHeader?.let {
                val notifications = result.notifications.map { TaskNotification.from(it) }
                val documentNotifications = result.documentNotifications.map { TaskNotification.from(it) }
                val productNotifications = result.productNotifications.map { TaskNotification.from(it) }
                val conditionNotifications = result.conditionNotifications.map { TaskNotification.from(it) }
                val deliveryDocumentsRevise = result.deliveryDocumentsRevise.map { DeliveryDocumentRevise.from(it) }.toMutableList()
//            deliveryDocumentsRevise.add(DeliveryDocumentRevise(documentID = "123", documentName = "Простой 1", documentType = DocumentType.Simple, isCheck = false, isObligatory = false))
//            deliveryDocumentsRevise.add(DeliveryDocumentRevise(documentID = "124", documentName = "Простой 2", documentType = DocumentType.Simple, isCheck = false, isObligatory = true))
//            deliveryDocumentsRevise.add(DeliveryDocumentRevise(documentID = "125", documentName = "Простой 3", documentType = DocumentType.Simple, isCheck = false, isObligatory = false))

                val deliveryProductDocumentsRevise = result.deliveryProductDocumentsRevise.map { DeliveryProductDocumentRevise.from(hyperHive, it) }.toMutableList()
//            deliveryProductDocumentsRevise.add(DeliveryProductDocumentRevise(documentID = "123", documentName = "Простой 1", documentType = ProductDocumentType.Simple, isCheck = false, isObligatory = false, initialCount = 1.0, isSet = false, productNumber = "000123", measureUnits = "ШТ"))
//            deliveryProductDocumentsRevise.add(DeliveryProductDocumentRevise(documentID = "124", documentName = "Простой 2", documentType = ProductDocumentType.Simple, isCheck = false, isObligatory = true, initialCount = 1.0, isSet = false, productNumber = "000124", measureUnits = "ШТ"))
//            deliveryProductDocumentsRevise.add(DeliveryProductDocumentRevise(documentID = "125", documentName = "Простой 3", documentType = ProductDocumentType.Simple, isCheck = false, isObligatory = false, initialCount = 1.0, isSet = false, productNumber = "000125", measureUnits = "ШТ"))

                val productBatchesRevise = result.productBatchesRevise.map { ProductBatchRevise.from(it) }
                val formsABRussianRevise = result.formsABRussianRevise.map { FormABRussianRevise.from(it) }
                val formsABImportRevise = result.formsABImportRevise.map { FormABImportRevise.from(it) }
                val setComponentsRevise = result.setComponentsRevise.map { SetComponentRevise.from(it) }
                val invoiceRevise = InvoiceRevise.from(result.invoiceRevise)
                val commentsToVP = result.commentsToVP.map { CommentToVP.from(it) }.toMutableList()
                val productsVetDocumentRevise = result.productsVetDocumentRevise.map { ProductVetDocumentRevise.from(hyperHive, it) }
                val complexDocumentsRevise = result.complexDocumentsRevise.map { ComplexDocumentRevise.from(it) }
                val transportConditions = result.transportConditions.map { TransportCondition.from(it) }

                val sectionInfo = result.sectionsInfo.map { TaskSectionInfo.from(it) }
                val sectionProducts = result.sectionProducts.map { TaskSectionProducts.from(it) }

                val newTask = taskManager.newReceivingTask(taskHeader, TaskDescription.from(result.taskDescription))
                newTask?.taskRepository?.getNotifications()?.updateWithNotifications(notifications, documentNotifications, productNotifications, conditionNotifications)
                newTask?.taskRepository?.getNotifications()?.updateWithInvoiceNotes(commentsToVP)
                newTask?.taskRepository?.getReviseDocuments()?.apply {
                    this.updateDeliveryDocuments(deliveryDocumentsRevise)
                    this.updateProductDocuments(deliveryProductDocumentsRevise)
                    this.updateImportABForms(formsABImportRevise)
                    this.updateRussianABForms(formsABRussianRevise)
                    this.updateProductBatches(productBatchesRevise)
                    this.updateSetComponents(setComponentsRevise)
                    this.updateInvoiceInfo(invoiceRevise)
                    this.updateTransportCondition(transportConditions)
                    this.updateProductVetDocuments(productsVetDocumentRevise)
                    this.updateComplexDocuments(complexDocumentsRevise)
                }
                taskManager.getReceivingTask()?.updateTaskWithContents(taskContents.getTaskContentsInfo(result))
                newTask?.taskRepository?.getSections()?.updateSections(sectionInfo, sectionProducts)
                taskManager.setTask(newTask)
                transferToNextScreen()
            }
        }

    }

    private fun transferToNextScreen() {
        taskManager.getReceivingTask()?.let { task ->
            when (task.taskDescription.currentStatus) {
                TaskStatus.Checking -> {
                    if (task.taskRepository.getReviseDocuments().getDeliveryDocuments().isNotEmpty()) {
                        screenNavigator.openTaskReviseScreen()
                    } else if (task.taskRepository.getReviseDocuments().getProductDocuments().isNotEmpty()) {
                        screenNavigator.openProductDocumentsReviseScreen()
                    } else {
                        screenNavigator.openTaskListScreen()
                        screenNavigator.openCheckingNotNeededAlert(context.getString(R.string.revise_not_needed_checking)) {
                            screenNavigator.openFinishReviseLoadingScreen()
                        }
                    }
                }
                TaskStatus.Unloading -> {
                    if (task.taskRepository.getReviseDocuments().getTransportConditions().isNotEmpty()) {
                        screenNavigator.openTransportConditionsScreen()
                    } else {
                        screenNavigator.openTaskListScreen()
                        screenNavigator.openCheckingNotNeededAlert(context.getString(R.string.revise_not_needed_unloading)) {
                            screenNavigator.openFinishConditionsReviseLoadingScreen()
                        }
                    }
                }
                TaskStatus.Recounting -> {
                    screenNavigator.openGoodsListScreen()
                }
                else -> {
                    screenNavigator.openTaskCardScreen(TaskCardMode.Full)
                }
            }
        }
    }

    override fun clean() {
        progress.postValue(false)
    }
}