package com.lenta.bp9.features.mercury_list_irrelevant

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.loading.tasks.TaskCardMode
import com.lenta.bp9.features.reject.RejectType
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskContents
import com.lenta.bp9.model.task.TaskDescription
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.network.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.toStringFormatted
import kotlinx.coroutines.launch
import javax.inject.Inject

const val ZMP_UTZ_GRZ_11_V001 = 11
const val ZMP_UTZ_GRZ_13_V001 = 13

class MercuryListIrrelevantViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var directSupplierStartRecountNetRequest: DirectSupplierStartRecountNetRequest
    @Inject
    lateinit var transmittedNetRequest: TransmittedNetRequest
    @Inject
    lateinit var taskContents: TaskContents
    @Inject
    lateinit var rejectRequest: RejectNetRequest
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    val listIrrelevantMercury: MutableLiveData<List<MercuryListIrrelevantItem>> = MutableLiveData()
    val netRestNumber: MutableLiveData<Int> = MutableLiveData()

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    fun onResume() {
        updateListIrrelevant()
    }

    private fun updateListIrrelevant() {
        taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getMercuryNotActual().let {listMercuryNotActual ->
            listIrrelevantMercury.postValue(
                    listMercuryNotActual?.mapIndexed { index, mercuryNotActual ->
                        MercuryListIrrelevantItem(
                                number = index + 1,
                                name = "${mercuryNotActual.getMaterialLastSix()} ${mercuryNotActual.productName}",
                                quantityWithUom = "- ${mercuryNotActual.volume.toStringFormatted()} ${mercuryNotActual.uom.name}",
                                even = index % 2 == 0)
                    }
            )
        }
    }

    fun onClickUntied() {
        when (netRestNumber.value) {
            ZMP_UTZ_GRZ_11_V001 -> {
                viewModelScope.launch {
                    screenNavigator.showProgressLoadingData()

                    taskManager.getReceivingTask()?.let { task ->
                        task.taskRepository.getReviseDocuments().let {taskDocs ->
                            taskDocs.getProductVetDocuments().map {
                                taskDocs.changeProductVetDocumentStatus(it, false)
                            }
                        }
                        val params = DirectSupplierStartRecountParams(
                                taskNumber = task.taskHeader.taskNumber,
                                deviceIP = context.getDeviceIp(),
                                personnelNumber = sessionInfo.personnelNumber ?: "",
                                dateRecount = task.taskDescription.currentStatusDate,
                                timeRecount = task.taskDescription.currentStatusTime,
                                unbindVSD = "X"
                        )
                        directSupplierStartRecountNetRequest(params).either(::handleFailure, ::handleSuccessRecountStart)
                    }
                    screenNavigator.hideProgress()
                }
            }
            ZMP_UTZ_GRZ_13_V001 -> {
                viewModelScope.launch {
                    screenNavigator.showProgressLoadingData()
                    taskManager.getReceivingTask()?.let { task ->
                        val params = TransmittedParams(
                                taskNumber = task.taskHeader.taskNumber,
                                deviceIP = context.getDeviceIp(),
                                personnelNumber = sessionInfo.personnelNumber ?: "",
                                unbindVSD = "X"
                        )
                        transmittedNetRequest(params).either(::handleFailure, ::handleSuccessTransmitted)
                    }
                    screenNavigator.hideProgress()
                }
            }
        }
    }

    private fun handleSuccessRecountStart(result: DirectSupplierStartRecountRestInfo) {
        repoInMemoryHolder.manufacturers.value = result.manufacturers
        taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))
        taskManager.getReceivingTask()?.updateTaskWithContents(taskContents.getTaskContentsInfo(result))
        screenNavigator.openGoodsListScreen()
    }

    private fun handleSuccessTransmitted(result: TransmittedRestInfo) {
        taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))
        screenNavigator.openTaskCardScreen(TaskCardMode.Full)
    }

    fun onClickTemporary() {
        viewModelScope.launch {
            screenNavigator.showProgress(rejectRequest)
            val params = RejectRequestParameters(
                    deviceIP = context.getDeviceIp(),
                    personalNumber = sessionInfo.personnelNumber ?: "",
                    taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber ?: "",
                    rejectMode = "1",
                    rejectReason = ""
            )
            rejectRequest(params).either(::handleFailure, ::handleSuccessReject)
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccessReject(result: RejectRequestResult) {
        screenNavigator.openMainMenuScreen()
        screenNavigator.openTaskListScreen()
    }
}
