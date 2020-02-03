package com.lenta.bp9.features.loading.tasks

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.model.task.*
import com.lenta.bp9.model.task.revise.TransportCondition
import com.lenta.bp9.model.task.revise.TransportConditionRestData
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.network.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.getDeviceIp
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoadingShipmentStartViewModel : CoreLoadingViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var zmpUtzGrz37V001NetRequest: ZmpUtzGrz37V001NetRequest
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    var taskNumber: String = ""

    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    val taskDescription: String by lazy {
        "\"" + (taskManager.getReceivingTask()?.taskDescription?.currentStatus?.stringValue() ?: "") + "\" -> \"" + taskManager.getReceivingTask()?.taskDescription?.nextStatusText + "\""
    }

    init {
        viewModelScope.launch {
            progress.value = true
            taskManager.getReceivingTask()?.let { task ->
                val params = ZmpUtzGrz37V001Params(
                        deviceIP = context.getDeviceIp(),
                        taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber ?: "",
                        personalNumber = sessionInfo.personnelNumber ?: "",
                        loadingStartDate = taskManager.getReceivingTask()?.taskDescription?.nextStatusDate ?: "",
                        loadingStartTime = taskManager.getReceivingTask()?.taskDescription?.nextStatusTime ?: ""
                )
                zmpUtzGrz37V001NetRequest(params).either(::handleFailure, ::handleSuccess)
            }
            progress.value = false
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.goBack()
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleSuccess(result: ZmpUtzGrz37V001Result) {
        viewModelScope.launch {
            val taskHeader = repoInMemoryHolder.taskList.value?.tasks?.findLast { it.taskNumber == taskNumber }
            taskHeader?.let {
                val notifications = result.notifications.map { TaskNotification.from(it) }
                val conditionNotifications = result.conditionNotifications.map { TaskNotification.from(it) }
                val transportConditions = result.transportConditions.map { TransportCondition.from(it) }

                val newTask = taskManager.newReceivingTask(taskHeader, TaskDescription.from(result.taskDescription))
                newTask?.taskRepository?.getNotifications()?.updateWithNotifications(notifications, null, null, conditionNotifications)
                newTask?.taskRepository?.getReviseDocuments()?.updateTransportCondition(transportConditions)
                taskManager.setTask(newTask)

                /** На карточке задания в статусе "Прибыло" (CUR_STAT=4) , при вызове интерфейса ZMP_UTZ_GRZ_37_V001, проверять наличие записей в таблицах ET_COND_CHECK и ET_COND_NOTIFY*/
                if (result.conditionNotifications.isEmpty() && result.transportConditions.isEmpty()) {
                    screenNavigator.openCheckingNotNeededAlert(context.getString(R.string.revise_not_needed)) {
                        screenNavigator.openTaskCardScreen(TaskCardMode.Full)
                    }
                } else {
                    screenNavigator.openTransportConditionsScreen() //экран Контроль условий перевозки
                }
            }
        }
    }

    override fun clean() {
        progress.postValue(false)
    }
}