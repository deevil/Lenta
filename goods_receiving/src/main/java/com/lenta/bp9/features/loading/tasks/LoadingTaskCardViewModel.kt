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
    lateinit var taskContentsReceptionDistrCenterNetRequest: TaskContentsReceptionDistrCenterNetRequest
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
            val taskHeader = repoInMemoryHolder.taskList.value?.tasks?.findLast { it.taskNumber == taskNumber }
            if (loadFullData) {
                when (taskHeader?.taskType) {
                    TaskType.ReceptionDistributionCenter -> {
                        if (taskHeader.status == TaskStatus.Traveling) {
                            taskCardNetRequest(params).either(::handleFailure, ::handleSuccess)
                        } else {
                            val paramsRDS = TaskContentsReceptionDistrCenterParameters(
                                    mode = mode.TaskCardModeString,
                                    deviceIP = context.getDeviceIp(),
                                    personalNumber = sessionInfo.personnelNumber ?: "",
                                    taskNumber = taskNumber,
                                    taskType = TaskType.ReceptionDistributionCenter.taskTypeString
                            )
                            taskContentsReceptionDistrCenterNetRequest(paramsRDS).either(::handleFailure, ::handleSuccessRDS)
                        }
                    }
                    TaskType.RecalculationCargoUnit -> {
                        if (taskHeader.status == TaskStatus.Unloaded) {
                            taskCardNetRequest(params).either(::handleFailure, ::handleSuccess)
                        } else {
                            val params = TaskContentsReceptionDistrCenterParameters(
                                    mode = mode.TaskCardModeString,
                                    deviceIP = context.getDeviceIp(),
                                    personalNumber = sessionInfo.personnelNumber ?: "",
                                    taskNumber = taskNumber,
                                    taskType = TaskType.RecalculationCargoUnit.taskTypeString
                            )
                            taskContentsReceptionDistrCenterNetRequest(params).either(::handleFailure, ::handleSuccessRDS)
                        }
                    }
                    else -> taskContentsNetRequest(params).either(::handleFailure, ::handleFullDataSuccess)
                }

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
        val taskHeader = repoInMemoryHolder.taskList.value?.tasks?.findLast { it.taskNumber == taskNumber } ?: repoInMemoryHolder.lastSearchResult.value?.tasks?.findLast { it.taskNumber == taskNumber }
        taskHeader?.let {
            val notifications = result.notifications.map { TaskNotification.from(it) }
            val newTask = taskManager.newReceivingTask(taskHeader, TaskDescription.from(result.taskDescription))
            newTask?.taskRepository?.getNotifications()?.updateWithNotifications(notifications, null, null, null)
            taskManager.setTask(newTask)
            screenNavigator.openTaskCardScreen(mode)
        }
    }

    private fun handleFullDataSuccess(result: TaskContentsRequestResult) {
        Logg.d { "Task card request result ${result}" }
        //screenNavigator.goBack()
        viewModelScope.launch {
            val taskHeader = repoInMemoryHolder.taskList.value?.tasks?.findLast { it.taskNumber == taskNumber }
            taskHeader?.let {
                val notifications = result.notifications.map { TaskNotification.from(it) }
                val documentNotifications = result.documentNotifications.map { TaskNotification.from(it) }
                val productNotifications = result.productNotifications.map { TaskNotification.from(it) }
                val conditionNotifications = result.conditionNotifications.map { TaskNotification.from(it) }
                val deliveryDocumentsRevise = result.deliveryDocumentsRevise.map { DeliveryDocumentRevise.from(it) }.toMutableList()
                val deliveryProductDocumentsRevise = result.deliveryProductDocumentsRevise.map { DeliveryProductDocumentRevise.from(hyperHive, it) }.toMutableList()
                val productBatchesRevise = result.productBatchesRevise.map { ProductBatchRevise.from(it) }
                val formsABRussianRevise = result.formsABRussianRevise.map { FormABRussianRevise.from(it) }
                val formsABImportRevise = result.formsABImportRevise.map { FormABImportRevise.from(it) }
                val setComponentsRevise = result.setComponentsRevise.map { SetComponentRevise.from(it) }
                val invoiceRevise = InvoiceRevise.from(result.invoiceRevise)
                val commentsToVP = result.commentsToVP.map { CommentToVP.from(it) }.toMutableList()
                val productsVetDocumentRevise = result.productsVetDocumentRevise.map { ProductVetDocumentRevise.from(hyperHive, it) }
                val complexDocumentsRevise = result.complexDocumentsRevise.map { ComplexDocumentRevise.from(it) }
                val transportConditions = result.transportConditions.map { TransportCondition.from(it) }
                val mercuryNotActual = result.taskMercuryNotActualRestData.map { TaskMercuryNotActual.from(hyperHive, it) }

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
                    this.updateMercuryNotActual(mercuryNotActual)
                }
                newTask?.updateTaskWithContents(taskContents.getTaskContentsInfo(result))
                newTask?.taskRepository?.getSections()?.updateSections(sectionInfo, sectionProducts)
                taskManager.setTask(newTask)
                transferToNextScreen()
            }
        }

    }

    private fun handleSuccessRDS(result: TaskContentsReceptionDistrCenterResult) {
        Logg.d { "handleSuccessRDS $result" }
        //screenNavigator.goBack()
        viewModelScope.launch {
            val taskHeader = repoInMemoryHolder.taskList.value?.tasks?.findLast { it.taskNumber == taskNumber }
            taskHeader?.let {
                val notifications = result.notifications.map { TaskNotification.from(it) }
                val documentNotifications = result.documentNotifications.map { TaskNotification.from(it) }
                val productNotifications = result.productNotifications.map { TaskNotification.from(it) }
                val conditionNotifications = result.conditionNotifications.map { TaskNotification.from(it) }
                val deliveryDocumentsRevise = result.deliveryDocumentsRevise.map { DeliveryDocumentRevise.from(it) }.toMutableList()
                val deliveryProductDocumentsRevise = result.deliveryProductDocumentsRevise.map { DeliveryProductDocumentRevise.from(hyperHive, it) }.toMutableList()
                val productBatchesRevise = result.productBatchesRevise.map { ProductBatchRevise.from(it) }
                val formsABRussianRevise = result.formsABRussianRevise.map { FormABRussianRevise.from(it) }
                val formsABImportRevise = result.formsABImportRevise.map { FormABImportRevise.from(it) }
                val setComponentsRevise = result.setComponentsRevise.map { SetComponentRevise.from(it) }
                val invoiceRevise = InvoiceRevise.from(result.invoiceRevise)
                //val commentsToVP = result.commentsToVP.map { CommentToVP.from(it) }.toMutableList()
                //val productsVetDocumentRevise = result.productsVetDocumentRevise.map { ProductVetDocumentRevise.from(hyperHive, it) }
                //val complexDocumentsRevise = result.complexDocumentsRevise.map { ComplexDocumentRevise.from(it) }
                val transportConditions = result.transportConditions.map { TransportCondition.from(it) }
                //val mercuryNotActual = result.taskMercuryNotActualRestData.map { TaskMercuryNotActual.from(hyperHive, it) }

                val sectionInfo = result.sectionsInfo.map { TaskSectionInfo.from(it) }
                val sectionProducts = result.sectionProducts.map { TaskSectionProducts.from(it) }

                val cargoUnits = result.cargoUnits.map { TaskCargoUnitInfo.from(it) }

                val newTask = taskManager.newReceivingTask(taskHeader, TaskDescription.from(result.taskDescription).copy(quantityOutgoingFillings = result.quantityOutgoingFillings.trim().toInt()))
                newTask?.taskRepository?.getNotifications()?.updateWithNotifications(notifications, documentNotifications, productNotifications, conditionNotifications)
                //newTask?.taskRepository?.getNotifications()?.updateWithInvoiceNotes(commentsToVP)
                newTask?.taskRepository?.getReviseDocuments()?.apply {
                    this.updateDeliveryDocuments(deliveryDocumentsRevise)
                    this.updateProductDocuments(deliveryProductDocumentsRevise)
                    this.updateImportABForms(formsABImportRevise)
                    this.updateRussianABForms(formsABRussianRevise)
                    this.updateProductBatches(productBatchesRevise)
                    this.updateSetComponents(setComponentsRevise)
                    this.updateInvoiceInfo(invoiceRevise)
                    this.updateTransportCondition(transportConditions)
                    //this.updateProductVetDocuments(productsVetDocumentRevise)
                    //this.updateComplexDocuments(complexDocumentsRevise)
                    //this.updateMercuryNotActual(mercuryNotActual)
                }
                newTask?.taskRepository?.getCargoUnits()?.updateCargoUnits(cargoUnits)
                newTask?.updateTaskWithContentsRDS(taskContents.getTaskContentsRDSInfo(result))
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