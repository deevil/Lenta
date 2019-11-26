package com.lenta.bp9.features.loading.tasks

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskDescription
import com.lenta.bp9.model.task.TaskNotification
import com.lenta.bp9.model.task.revise.*
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.FinishReviseNetRequest
import com.lenta.bp9.requests.network.FinishReviseRequestParameters
import com.lenta.bp9.requests.network.FinishReviseRequestResult
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.getDeviceIp
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoadingFinishReviseViewModel : CoreLoadingViewModel() {
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var finishReviseRequest: FinishReviseNetRequest
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
            taskManager.getReceivingTask()?.let { task ->
                val params = FinishReviseRequestParameters(
                        deviceIP = context.getDeviceIp(),
                        personalNumber = sessionInfo.personnelNumber ?: "",
                        taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber ?: "",
                        deliveryReviseDocuments = task.taskRepository.getReviseDocuments().getDeliveryDocuments().map { DeliveryDocumentReviseRestData.from(it) },
                        productReviseDocuments = task.taskRepository.getReviseDocuments().getProductDocuments().map { DeliveryProductDocumentReviseRestData.from(it) },
                        productBatches = task.taskRepository.getReviseDocuments().getProductBatches().map { ProductBatchReviseRestData.from(it) },
                        russianABForms = task.taskRepository.getReviseDocuments().getRussianABForms().map { FormABRussianReviseRestData.from(it) },
                        importABForms = task.taskRepository.getReviseDocuments().getImportABForms().map { FormABImportReviseRestData.from(it) },
                        invoiceData = InvoiceReviseRestData.from(task.taskRepository.getReviseDocuments().getInvoiceInfo()),
                        commentsToVP = task.taskRepository.getNotifications().getInvoiceNotes().map { CommentToVPRestData.from(it) },
                        productVetDocument = task.taskRepository.getReviseDocuments().getProductVetDocuments().map { ProductVetDocumentReviseRestData.from(it) }
                )
                finishReviseRequest(params).either(::handleFailure, ::handleSuccess)
            }
            progress.value = false
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.goBack()
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleSuccess(result: FinishReviseRequestResult) {
        Logg.d { "Finish revise request result $result" }
        screenNavigator.openMainMenuScreen()
        screenNavigator.openTaskListScreen()
        taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))
        val notifications = result.notifications.map { TaskNotification.from(it) }
        taskManager.getReceivingTask()?.taskRepository?.getNotifications()?.updateWithNotifications(general = notifications, document = null, product = null, condition = null)
        screenNavigator.openTaskCardScreen(TaskCardMode.Full)
    }

    override fun clean() {
        progress.postValue(false)
    }
}