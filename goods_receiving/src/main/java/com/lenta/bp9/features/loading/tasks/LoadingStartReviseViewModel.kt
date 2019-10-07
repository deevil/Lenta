package com.lenta.bp9.features.loading.tasks

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskNotification
import com.lenta.bp9.model.task.revise.*
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.StartReviseNetRequest
import com.lenta.bp9.requests.network.StartReviseRequestParameters
import com.lenta.bp9.requests.network.StartReviseRequestResult
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.getDeviceIp
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoadingStartReviseViewModel : CoreLoadingViewModel() {
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var startReviseRequest: StartReviseNetRequest
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    init {
        viewModelScope.launch {
            progress.value = true
            val params = StartReviseRequestParameters(
                    deviceIP = context.getDeviceIp(),
                    personalNumber = sessionInfo.personnelNumber ?: "",
                    taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber ?: "",
                    arrivalDate = taskManager.getReceivingTask()?.taskDescription?.currentStatusDate ?: "",
                    arrivalTime = taskManager.getReceivingTask()?.taskDescription?.currentStatusTime ?: "",
                    reviseStartDate = taskManager.getReceivingTask()?.taskDescription?.nextStatusDate ?: "",
                    reviseStartTime = taskManager.getReceivingTask()?.taskDescription?.nextStatusTime ?: ""
            )
            startReviseRequest(params).either(::handleFailure, ::handleSuccess)
            progress.value = false
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.goBack()
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleSuccess(result: StartReviseRequestResult) {
        Logg.d { "Register arrival request result $result" }
        screenNavigator.goBack()
        screenNavigator.goBack()

        val documentNotifications = result.documentNotifications.map { TaskNotification.from(it) }
        val productNotifications = result.productNotifications.map { TaskNotification.from(it) }
        val commentsToVP = result.commentsToVP.map { CommentToVP.from(it) }
        taskManager.getReceivingTask()?.taskRepository?.getNotifications()?.updateWithNotifications(null, documentNotifications, productNotifications, null)
        taskManager.getReceivingTask()?.taskRepository?.getNotifications()?.updateWithInvoiceNotes(commentsToVP)

        val deliveryDocumentsRevise = result.deliveryReviseDocuments.map { DeliveryDocumentRevise.from(it) }
        val deliveryProductDocumentsRevise = result.productReviseDocuments.map { DeliveryProductDocumentRevise.from(it) }
        val productBatchesRevise = result.productBatches.map { ProductBatchRevise.from(it) }
        val formsABRussianRevise = result.russianABForms.map { FormABRussianRevise.from(it) }
        val formsABImportRevise = result.importABForms.map { FormABImportRevise.from(it) }
        val setComponenttsRevise = result.setComponents.map { SetComponentRevise.from(it) }
        val invoiceRevise = InvoiceRevise.from(result.invoiceData)
        taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.apply {
            this.updateDeliveryDocuments(deliveryDocumentsRevise)
            this.updateProductDocuments(deliveryProductDocumentsRevise)
            this.updateImportABForms(formsABImportRevise)
            this.updateRussianABForms(formsABRussianRevise)
            this.updateProductBatches(productBatchesRevise)
            this.updateSetComponents(setComponenttsRevise)
            this.updateInvoiceInfo(invoiceRevise)
        }
        taskManager.getReceivingTask()?.let { task ->
            if (task.taskRepository.getReviseDocuments().getDeliveryDocuments().isNotEmpty()) {
                screenNavigator.openTaskReviseScreen()
            } else if (task.taskRepository.getReviseDocuments().getProductDocuments().isNotEmpty()) {
                screenNavigator.openProductDocumentsReviseScreen()
            } else {
                screenNavigator.openCheckingNotNeededAlert {
                    screenNavigator.openFinishReviseLoadingScreen()
                }
            }
        }
    }

    override fun clean() {
        progress.postValue(false)
    }
}