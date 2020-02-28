package com.lenta.bp9.features.loading.tasks

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.mercury_list_irrelevant.ZMP_UTZ_GRZ_11_V001
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.network.DirectSupplierStartRecountNetRequest
import com.lenta.bp9.requests.network.DirectSupplierStartRecountParams
import com.lenta.bp9.requests.network.DirectSupplierStartRecountRestInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoadingRecountStartViewModel : CoreLoadingViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var directSupplierStartRecountNetRequest: DirectSupplierStartRecountNetRequest
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var taskContents: TaskContents
    @Inject
    lateinit var hyperHive: HyperHive
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    val toolbarDescription: String by lazy {
        if (taskManager.getReceivingTask()?.taskDescription?.currentStatus == TaskStatus.Recounted) {
            "\"" + TaskStatus.Recounted.stringValue() + "\" -> \"" + TaskStatus.Recounting.stringValue() + "\""
        } else {
            "\"" + (taskManager.getReceivingTask()?.taskDescription?.currentStatus?.stringValue() ?: "") + "\" -> \"" + TaskStatus.Recounting.stringValue() + "\""
        }
    }

    init {
        viewModelScope.launch {
            progress.value = true
            taskManager.getReceivingTask()?.let { task ->
                val params = DirectSupplierStartRecountParams(
                        taskNumber = task.taskHeader.taskNumber,
                        deviceIP = context.getDeviceIp(),
                        personnelNumber = sessionInfo.personnelNumber ?: "",
                        dateRecount = task.taskDescription.currentStatusDate,
                        timeRecount = task.taskDescription.currentStatusTime,
                        unbindVSD = ""
                )
                directSupplierStartRecountNetRequest(params).either(::handleFailure, ::handleSuccess)
            }
            progress.value = false
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.goBack()
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleSuccess(result: DirectSupplierStartRecountRestInfo) {
        viewModelScope.launch {
            repoInMemoryHolder.manufacturers.value = result.manufacturers
            val mercuryNotActual = result.taskMercuryNotActualRestData.map {TaskMercuryNotActual.from(hyperHive,it)}
            if (mercuryNotActual.isNotEmpty()) {
                screenNavigator.openMainMenuScreen()
                screenNavigator.openTaskListScreen()
                screenNavigator.openAlertElectronicVadLostRelevance(
                        browsingCallbackFunc = {
                            taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.updateMercuryNotActual(mercuryNotActual)
                            screenNavigator.openMercuryListIrrelevantScreen(ZMP_UTZ_GRZ_11_V001)
                        },
                        countVad = mercuryNotActual.size.toString(),
                        countGoods = taskManager.getReceivingTask()?.taskDescription?.quantityPositions.toString()
                )
            } else {
                taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))
                taskManager.getReceivingTask()?.updateTaskWithContents(taskContents.getTaskContentsInfo(result))
                screenNavigator.openGoodsListScreen()
            }
        }
    }

    override fun clean() {
        progress.postValue(false)
    }
}