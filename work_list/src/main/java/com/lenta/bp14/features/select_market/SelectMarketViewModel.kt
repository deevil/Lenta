package com.lenta.bp14.features.select_market

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lenta.bp14.models.IGeneralTaskManager
import com.lenta.bp14.models.ITask
import com.lenta.bp14.models.ITaskDescription
import com.lenta.bp14.models.ITaskManager
import com.lenta.bp14.models.check_list.CheckListData
import com.lenta.bp14.models.check_list.CheckListTaskManager
import com.lenta.bp14.models.general.AppTaskTypes
import com.lenta.bp14.models.work_list.WorkListData
import com.lenta.bp14.models.work_list.WorkListTaskManager
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.repos.IRepoInMemoryHolder
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.printer_change.PrinterManager
import com.lenta.shared.models.core.StateFromToString
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.network.ServerTime
import com.lenta.shared.requests.network.ServerTimeRequest
import com.lenta.shared.requests.network.ServerTimeRequestParam
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.extentions.implementationOf
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class SelectMarketViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var appSettings: IAppSettings
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder
    @Inject
    lateinit var timeMonitor: ITimeMonitor
    @Inject
    lateinit var serverTimeRequest: ServerTimeRequest
    @Inject
    lateinit var printerManager: PrinterManager
    @Inject
    lateinit var generalTaskManager: IGeneralTaskManager
    @Inject
    lateinit var checkListTaskManager: CheckListTaskManager
    @Inject
    lateinit var workListTaskManager: WorkListTaskManager
    @Inject
    lateinit var gson: Gson


    private val markets: MutableLiveData<List<MarketUi>> = MutableLiveData()
    val marketsNames: MutableLiveData<List<String>> = markets.map { markets ->
        markets?.map { it.number }
    }
    val selectedPosition: MutableLiveData<Int> = MutableLiveData()
    val selectedAddress: MutableLiveData<String> = selectedPosition.map {
        it?.let { position ->
            markets.value?.getOrNull(position)?.address
        }
    }

    init {
        viewModelScope.launch {
            repoInMemoryHolder.storesRequestResult?.markets?.let { list ->
                markets.value = list.map { MarketUi(number = it.tkNumber, address = it.address) }

                if (selectedPosition.value == null) {
                    if (appSettings.lastTK != null) {
                        list.forEachIndexed { index, market ->
                            if (market.tkNumber == appSettings.lastTK) {
                                onClickPosition(index)
                            }
                        }
                    } else {
                        onClickPosition(0)
                    }
                }

                if (list.size == 1) {
                    onClickNext()
                }
            }
        }
    }

    fun onClickNext() {
        viewModelScope.launch {
            markets.value?.getOrNull(selectedPosition.value ?: -1)?.number?.let { tkNumber ->
                if (appSettings.lastTK != tkNumber) {
                    printerManager.setDefaultPrinterForTk(tkNumber)
                }
                sessionInfo.market = tkNumber
                appSettings.lastTK = tkNumber
                navigator.showProgress(serverTimeRequest)
                serverTimeRequest(ServerTimeRequestParam(sessionInfo.market
                        ?: "")).either(::handleFailure, ::handleSuccessServerTime)
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.hideProgress()
    }

    private fun handleSuccessServerTime(serverTime: ServerTime) {
        navigator.hideProgress()
        timeMonitor.setServerTime(time = serverTime.time, date = serverTime.date)


        // Раскомментировать для удаление сохраненных данных
        //generalTaskManager.clearSavedTaskData()

        if (generalTaskManager.isExistSavedTaskData()) {
            navigator.showUnsavedDataFoundOnDevice(
                    deleteCallback = {
                        generalTaskManager.clearSavedTaskData()
                        navigator.openMainMenuScreen()
                    },
                    goOverCallback = {
                        restoreSavedTask()
                    }
            )
        } else {
            navigator.openMainMenuScreen()
        }
    }

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }


    private fun restoreSavedTask() {
        navigator.showProgressLoadingData()

        generalTaskManager.getSavedData()?.let { taskData ->
            when (taskData.taskType) {
                /*AppTaskTypes.CheckPrice.taskType -> {
                    if (taskFromTaskList != null) {
                        checkPriceTaskInfoNetRequest(
                                CheckPriceTaskInfoParams(
                                        taskNumber = taskFromTaskList!!.taskId,
                                        ip = deviceInfo.getDeviceIp(),
                                        withProductInfo = true.toSapBooleanString(),
                                        mode = "1"
                                )
                        )
                    } else {
                        Either.Right(null)
                    }.let {
                        it.either({ failure ->
                            navigator.openAlertScreen(failure)
                        }) { checkPriceTaskInfoResult ->
                            newTask(
                                    taskManager = checkPriceTaskManager,
                                    taskDescription = CheckPriceTaskDescription(
                                            tkNumber = sessionInfo.market!!,
                                            taskNumber = taskFromTaskList?.taskId
                                                    ?: "",
                                            taskName = taskName.value ?: "",
                                            comment = comment.value ?: "",
                                            description = description.value ?: "",
                                            isStrictList = taskFromTaskList?.isStrict ?: false,
                                            additionalTaskInfo = checkPriceTaskInfoResult
                                    )
                            )
                            navigator.openGoodsListPcScreen()
                        }

                    }

                }*/

                AppTaskTypes.CheckList.taskType -> {
                    val data = gson.fromJson(taskData.data, CheckListData::class.java)
                    newTask(
                            taskName = data.taskDescription.taskName,
                            taskManager = checkListTaskManager,
                            taskDescription = data.taskDescription
                    )
                    checkListTaskManager.getTask().implementationOf(StateFromToString::class.java)?.restoreData(data)
                    navigator.openGoodsListClScreen()
                }

                AppTaskTypes.WorkList.taskType -> {
                    val data = gson.fromJson(taskData.data, WorkListData::class.java)
                    newTask(
                            taskName = data.taskDescription.taskName,
                            taskManager = workListTaskManager,
                            taskDescription = data.taskDescription
                    )
                    workListTaskManager.getTask().implementationOf(StateFromToString::class.java)?.restoreData(data)
                    navigator.openGoodsListWlScreen()
                }

                /*AppTaskTypes.NotExposedProducts.taskType -> {
                    if (taskFromTaskList != null) {
                        notExposedTaskInfoNetRequest(
                                NotExposedTaskInfoParams(
                                        taskNumber = taskFromTaskList!!.taskId,
                                        ip = deviceInfo.getDeviceIp(),
                                        withProductInfo = true.toSapBooleanString(),
                                        mode = "1"
                                )
                        )
                    } else {
                        Either.Right(null)
                    }.let {
                        it.either({ failure ->
                            navigator.openAlertScreen(failure)
                        }) { result ->
                            newTask(
                                    taskManager = notExposedProductsTaskManager,
                                    taskDescription = NotExposedProductsTaskDescription(
                                            tkNumber = sessionInfo.market!!,
                                            taskNumber = taskFromTaskList?.taskId
                                                    ?: "",
                                            taskName = taskName.value ?: "",
                                            comment = comment.value ?: "",
                                            description = description.value ?: "",
                                            isStrictList = taskFromTaskList?.isStrict ?: false,
                                            additionalTaskInfo = result
                                    )
                            )
                            navigator.openGoodsListNeScreen()
                        }
                    }
                }*/
                else -> navigator.openNotImplementedScreenAlert("")
            }
        }

        navigator.hideProgress()
    }

    private fun <S : ITask, D : ITaskDescription> newTask(taskName: String, taskManager: ITaskManager<S, D>, taskDescription: D) {
        if (taskManager.getTask() == null) {
            taskManager.clearTask()
            taskManager.newTask(
                    taskDescription = taskDescription
            )
        } else {
            taskManager.getTask()?.getDescription()?.taskName = taskName
        }
        //updateProcessedTask()
    }

    /*private fun updateProcessedTask() {
        generalTaskManager.getProcessedTask().let { processedTask ->
            this.processedTask.value = generalTaskManager.getProcessedTask()
            val taskType = processedTask?.getTaskType()
                    ?: taskFromTaskList?.taskTypeInfo
            taskType?.let {
                taskTypesInfo.value?.apply {
                    for (pos in 0..this.size) {
                        if (it == this[pos]) {
                            selectedTaskTypePosition.value = pos
                            return
                        }
                    }
                }
            }
        }
    }*/

}

data class MarketUi(
        val number: String,
        val address: String
)