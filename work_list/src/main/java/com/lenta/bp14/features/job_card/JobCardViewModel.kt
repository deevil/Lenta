package com.lenta.bp14.features.job_card

import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.models.*
import com.lenta.bp14.models.check_list.CheckListTaskDescription
import com.lenta.bp14.models.check_list.CheckListTaskManager
import com.lenta.bp14.models.check_price.CheckPriceTaskDescription
import com.lenta.bp14.models.check_price.CheckPriceTaskManager
import com.lenta.bp14.models.general.AppTaskTypes
import com.lenta.bp14.models.general.IGeneralRepo
import com.lenta.bp14.models.general.ITaskTypeInfo
import com.lenta.bp14.models.general.ITasksSearchHelper
import com.lenta.bp14.models.not_exposed.NotExposedTaskDescription
import com.lenta.bp14.models.not_exposed.NotExposedTaskManager
import com.lenta.bp14.models.work_list.WorkListTaskDescription
import com.lenta.bp14.models.work_list.WorkListTaskManager
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.requests.check_price.CheckPriceTaskInfoParams
import com.lenta.bp14.requests.check_price.ICheckPriceTaskInfoNetRequest
import com.lenta.bp14.requests.not_exposed_product.NotExposedTaskInfoNetRequest
import com.lenta.bp14.requests.not_exposed_product.NotExposedTaskInfoParams
import com.lenta.bp14.requests.pojo.TaskInfoParams
import com.lenta.bp14.requests.tasks.UnlockTaskNetRequest
import com.lenta.bp14.requests.tasks.UnlockTaskParams
import com.lenta.bp14.requests.work_list.IWorkListTaskInfoNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toSapBooleanString
import com.lenta.shared.view.OnPositionClickListener
import javax.inject.Inject

class JobCardViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var generalRepo: IGeneralRepo
    @Inject
    lateinit var generalTaskManager: IGeneralTaskManager
    @Inject
    lateinit var checkPriceTaskManager: CheckPriceTaskManager
    @Inject
    lateinit var checkListTaskManager: CheckListTaskManager
    @Inject
    lateinit var notExposedTaskManager: NotExposedTaskManager
    @Inject
    lateinit var workListTaskManager: WorkListTaskManager
    @Inject
    lateinit var tasksSearchHelper: ITasksSearchHelper
    @Inject
    lateinit var deviceInfo: DeviceInfo
    @Inject
    lateinit var checkPriceTaskInfoNetRequest: ICheckPriceTaskInfoNetRequest
    @Inject
    lateinit var notExposedTaskInfoNetRequest: NotExposedTaskInfoNetRequest
    @Inject
    lateinit var workListTaskInfoNetRequest: IWorkListTaskInfoNetRequest
    @Inject
    lateinit var unlockTaskNetRequest: UnlockTaskNetRequest


    private val taskFromTaskList by lazy {
        tasksSearchHelper.processedTaskInfo
    }

    private val taskTypesInfo: MutableLiveData<List<ITaskTypeInfo>> = MutableLiveData(emptyList())
    private val processedTask: MutableLiveData<ITask> = MutableLiveData()

    val taskTypeNames: MutableLiveData<List<String>> = taskTypesInfo.map { it?.map { type -> type.taskName } }
    val selectedTaskTypePosition: MutableLiveData<Int> = MutableLiveData(0)
    private val selectedTaskTypeInfo: MutableLiveData<ITaskTypeInfo> = selectedTaskTypePosition.map { getSelectedTypeTask() }
    val enabledChangeTaskType: MutableLiveData<Boolean> = processedTask.map { it == null && taskFromTaskList == null }

    val isEnabledChangeTaskName: MutableLiveData<Boolean> = processedTask.map {
        it?.getDescription() == null && (taskFromTaskList == null || it?.isFreeMode() == true)
    }

    val isStrictList = MutableLiveData(false)

    val taskName by lazy {
        if (taskFromTaskList != null) {
            MutableLiveData(taskFromTaskList!!.taskName)

        } else {
            selectedTaskTypeInfo.map { selectedTaskType ->
                processedTask.value.let { task ->
                    task?.getDescription()?.taskName
                            ?: generalTaskManager.generateNewNameForTask(selectedTaskType)
                }
            }
        }

    }

    val description = selectedTaskTypeInfo.map { it?.annotation }

    val comment = selectedTaskTypeInfo.map { taskFromTaskList?.comment }

    val enabledNextButton = selectedTaskTypeInfo.map { it != null && it.taskType != AppTaskTypes.Empty.taskType }

    val onClickTaskTypes = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            selectedTaskTypePosition.value = position
        }
    }

    init {
        launchUITryCatch {
            taskTypesInfo.value = generalRepo.getTasksTypes()
            isStrictList.value = isStrictList()
            updateProcessedTask()

            generalTaskManager.apply {
                if (isRestoredData) {
                    isRestoredData = false
                    onClickNext()
                }
            }
        }
    }

    fun getMarket(): String {
        return sessionInfo.market!!
    }

    fun onClickNext() {
        when {
            taskFromTaskList?.isNotFinished == true -> openTask()
            taskFromTaskList?.isMyBlock == true -> {
                screenNavigator.showAlertBlockedTaskByMe {
                    openTask()
                }

            }
            taskFromTaskList?.isMyBlock == false -> {
                screenNavigator.showAlertBlockedTaskAnotherUser(taskFromTaskList!!.blockingUser)
            }

            else -> openTask()
        }
    }

    private fun openTask() {
        launchUITryCatch {
            screenNavigator.showProgressLoadingData(::handleFailure)
            when (getSelectedTypeTask()?.taskType) {
                AppTaskTypes.CheckPrice.taskType -> {
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
                    }.let { requestResult ->
                        requestResult.either({ failure ->
                            screenNavigator.openAlertScreen(failure)
                        }) { result ->
                            launchUITryCatch {
                                val missing = result?.positions?.filter {
                                    !generalRepo.isExistMaterial(it.matNr)
                                }?.map { it.matNr }?.toSet()

                                val correctedResult = if (missing?.isNotEmpty() == true) {
                                    result.copy(
                                            positions = result.positions.filter { !missing.contains(it.matNr) },
                                            checkPrices = result.checkPrices.filter { !missing.contains(it.matNr) },
                                            productsInfo = result.productsInfo.filter { !missing.contains(it.matNr) },
                                            prices = result.prices.filter { !missing.contains(it.matnr) }
                                    )
                                } else result

                                newTask(
                                        taskManager = checkPriceTaskManager,
                                        taskDescription = CheckPriceTaskDescription(
                                                tkNumber = sessionInfo.market!!,
                                                taskNumber = taskFromTaskList?.taskId.orEmpty(),
                                                taskName = taskName.value.orEmpty(),
                                                comment = comment.value.orEmpty(),
                                                description = description.value.orEmpty(),
                                                isStrictList = taskFromTaskList?.isStrict ?: false,
                                                additionalTaskInfo = correctedResult
                                        )
                                )
                                screenNavigator.openGoodsListPcScreen()
                            }
                        }
                    }
                }

                AppTaskTypes.CheckList.taskType -> {
                    newTask(
                            taskManager = checkListTaskManager,
                            taskDescription = CheckListTaskDescription(
                                    tkNumber = sessionInfo.market!!,
                                    taskNumber = taskFromTaskList?.taskId.orEmpty(),
                                    taskName = taskName.value.orEmpty(),
                                    comment = comment.value.orEmpty(),
                                    description = description.value.orEmpty(),
                                    isStrictList = taskFromTaskList?.isStrict ?: false
                            )
                    )
                    screenNavigator.openGoodsListClScreen()
                }

                AppTaskTypes.WorkList.taskType -> {
                    if (taskFromTaskList != null) {
                        workListTaskInfoNetRequest(
                                TaskInfoParams(
                                        taskNumber = taskFromTaskList!!.taskId,
                                        ip = deviceInfo.getDeviceIp(),
                                        withProductInfo = false.toSapBooleanString(),
                                        mode = "1"
                                )
                        )
                    } else {
                        Either.Right(null)
                    }.let { requestResult ->
                        requestResult.either({ failure ->
                            screenNavigator.openAlertScreen(failure)
                        }) { result ->
                            launchUITryCatch {
                                val missing = result?.positions?.filter {
                                    !generalRepo.isExistMaterial(it.matNr)
                                }?.map { it.matNr }?.toSet()

                                val correctedResult = if (missing?.isNotEmpty() == true) {
                                    result.copy(
                                            positions = result.positions.filter { !missing.contains(it.matNr) },
                                            additionalInfoList = result.additionalInfoList.filter { !missing.contains(it.matnr) },
                                            places = result.places.filter { !missing.contains(it.matnr) },
                                            suppliers = result.suppliers.filter { !missing.contains(it.matnr) },
                                            stocks = result.stocks.filter { !missing.contains(it.matnr) },
                                            checkResults = result.checkResults.filter { !missing.contains(it.matNr) },
                                            productsInfo = result.productsInfo.filter { !missing.contains(it.matNr) },
                                            marks = result.marks.filter { !missing.contains(it.matNr) }
                                    )
                                } else result

                                newTask(
                                        taskManager = workListTaskManager,
                                        taskDescription = WorkListTaskDescription(
                                                tkNumber = sessionInfo.market!!,
                                                taskNumber = taskFromTaskList?.taskId.orEmpty(),
                                                taskName = taskName.value.orEmpty(),
                                                comment = comment.value.orEmpty(),
                                                description = description.value.orEmpty(),
                                                isStrictList = taskFromTaskList?.isStrict ?: false,
                                                taskInfoResult = correctedResult
                                        )
                                )
                                screenNavigator.openGoodsListWlScreen()
                            }
                        }
                    }
                }

                AppTaskTypes.NotExposedProducts.taskType -> {
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
                    }.let { requestResult ->
                        requestResult.either({ failure ->
                            screenNavigator.openAlertScreen(failure)
                        }) { result ->
                            launchUITryCatch {
                                val missing = result?.positions?.filter {
                                    !generalRepo.isExistMaterial(it.matNr)
                                }?.map { it.matNr }?.toSet()

                                val correctedResult = if (missing?.isNotEmpty() == true) {
                                    result.copy(
                                            positions = result.positions.filter { !missing.contains(it.matNr) },
                                            stocks = result.stocks.filter { !missing.contains(it.matnr) },
                                            productsInfo = result.productsInfo.filter { !missing.contains(it.matNr) },
                                            checkPlaces = result.checkPlaces.filter { !missing.contains(it.matNr) }
                                    )
                                } else result

                                newTask(
                                        taskManager = notExposedTaskManager,
                                        taskDescription = NotExposedTaskDescription(
                                                tkNumber = sessionInfo.market!!,
                                                taskNumber = taskFromTaskList?.taskId
                                                       .orEmpty(),
                                                taskName = taskName.value.orEmpty(),
                                                comment = comment.value.orEmpty(),
                                                description = description.value.orEmpty(),
                                                isStrictList = taskFromTaskList?.isStrict ?: false,
                                                additionalTaskInfo = correctedResult
                                        )
                                )
                                screenNavigator.openGoodsListNeScreen()
                            }
                        }
                    }
                }

                else -> screenNavigator.openNotImplementedScreenAlert("")
            }

            screenNavigator.hideProgress()
        }
    }

    fun onBackPressed(): Boolean {
        if (generalTaskManager.getProcessedTask()?.isEmpty() != false) {
            clearCurrentTaskAndGoBack()
        } else {
            screenNavigator.openConfirmationExitTask(generalTaskManager.getProcessedTask()?.getDescription()?.taskName
                   .orEmpty()) {
                clearCurrentTaskAndGoBack()
            }
        }

        return false
    }

    private fun clearCurrentTaskAndGoBack() {
        launchUITryCatch {
            generalTaskManager.getProcessedTask()?.getTaskNumber().let { taskNumber ->
                if (taskNumber?.isNotBlank() == true) {
                    screenNavigator.showProgress(unlockTaskNetRequest)
                    tasksSearchHelper.isDataChanged = true
                    unlockTaskNetRequest(
                            UnlockTaskParams(
                                    ip = deviceInfo.getDeviceIp(),
                                    taskNumber = taskNumber
                            )
                    ).either(::handleFailure) {
                        generalTaskManager.clearCurrentTask()
                        tasksSearchHelper.processedTaskInfo = null
                        screenNavigator.goBack()
                        true
                    }
                    screenNavigator.hideProgress()
                } else {
                    generalTaskManager.clearCurrentTask()
                    tasksSearchHelper.processedTaskInfo = null
                    screenNavigator.goBack()
                }
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        screenNavigator.openAlertScreen(failure)
    }

    private fun <S : ITask, D : ITaskDescription> newTask(taskManager: ITaskManager<S, D>, taskDescription: D) {
        tasksSearchHelper.processedTaskInfo = null
        if (taskManager.getTask() == null) {
            taskManager.clearTask()
            taskManager.newTask(
                    taskDescription = taskDescription
            )
        } else {
            taskManager.getTask()?.getDescription()?.taskName = taskName.value!!
        }
        updateProcessedTask()
    }

    private fun getSelectedTypeTask(): ITaskTypeInfo? {
        return selectedTaskTypePosition.value?.let {
            taskTypesInfo.value?.getOrNull(it)
        }
    }

    private fun isStrictList(): Boolean {
        return taskFromTaskList?.isStrict ?: processedTask.value?.getDescription()?.isStrictList == true
    }

    private fun updateProcessedTask() {
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
    }

}
