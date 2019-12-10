package com.lenta.bp9.features.loading.tasks

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.mercury_list_irrelevant.ZMP_UTZ_GRZ_13_V001
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskDescription
import com.lenta.bp9.model.task.TaskMercuryNotActual
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.TransmittedNetRequest
import com.lenta.bp9.requests.network.TransmittedParams
import com.lenta.bp9.requests.network.TransmittedRestInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoadingTransmittedViewModel : CoreLoadingViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var transmittedNetRequest: TransmittedNetRequest
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var hyperHive: HyperHive

    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    init {
        viewModelScope.launch {
            progress.value = true
            taskManager.getReceivingTask()?.let { task ->
                val params = TransmittedParams(
                        taskNumber = task.taskHeader.taskNumber,
                        deviceIP = context.getDeviceIp(),
                        personnelNumber = sessionInfo.personnelNumber ?: "",
                        unbindVSD = ""
                )
                transmittedNetRequest(params).either(::handleFailure, ::handleSuccess)
            }
            progress.value = false
        }
    }

    private fun handleSuccess(result: TransmittedRestInfo) {
        viewModelScope.launch {
            val mercuryNotActual = result.taskMercuryNotActualRestData.map { TaskMercuryNotActual.from(hyperHive,it)}
            if (mercuryNotActual.isNotEmpty()) {
                screenNavigator.openMainMenuScreen()
                screenNavigator.openTaskListScreen()
                screenNavigator.openAlertElectronicVadLostRelevance(
                        browsingCallbackFunc = {
                            taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.updateMercuryNotActual(mercuryNotActual)
                            screenNavigator.openMercuryListIrrelevantScreen(ZMP_UTZ_GRZ_13_V001)
                        },
                        countVad = mercuryNotActual.size.toString(),
                        countGoods = taskManager.getReceivingTask()?.taskDescription?.quantityPositions.toString()
                )
            } else {
                taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))
                screenNavigator.openTaskCardScreen(TaskCardMode.Full)
            }
        }
    }

    override fun clean() {
        progress.postValue(false)
    }
}