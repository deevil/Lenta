package com.lenta.bp14.features.job_card

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.IGeneralTaskManager
import com.lenta.bp14.models.ITask
import com.lenta.bp14.models.ITaskDescription
import com.lenta.bp14.models.ITaskManager
import com.lenta.bp14.models.check_list.CheckListTaskDescription
import com.lenta.bp14.models.check_list.CheckListTaskManager
import com.lenta.bp14.models.check_price.CheckPriceTaskDescription
import com.lenta.bp14.models.check_price.CheckPriceTaskManager
import com.lenta.bp14.models.general.*
import com.lenta.bp14.models.not_exposed_products.NotExposedProductsTaskDescription
import com.lenta.bp14.models.not_exposed_products.NotExposedProductsTaskManager
import com.lenta.bp14.models.work_list.WorkListTaskDescription
import com.lenta.bp14.models.work_list.WorkListTaskManager
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.requests.check_price.CheckPriceTaskInfoParams
import com.lenta.bp14.requests.check_price.ICheckPriceTaskInfoNetRequest
import com.lenta.bp14.requests.not_exposed_product.NotExposedTaskInfoNetRequest
import com.lenta.bp14.requests.not_exposed_product.NotExposedTaskInfoParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.functional.Either
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toSapBooleanString
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
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
    lateinit var notExposedProductsTaskManager: NotExposedProductsTaskManager
    @Inject
    lateinit var workListTaskManager: WorkListTaskManager
    @Inject
    lateinit var tasksSearchHelper: ITasksSearchHelper
    @Inject
    lateinit var deviceIndo: DeviceInfo
    @Inject
    lateinit var checkPriceTaskInfoNetRequest: ICheckPriceTaskInfoNetRequest
    @Inject
    lateinit var notExposedTaskInfoNetRequest: NotExposedTaskInfoNetRequest


    private val taskFromTaskList by lazy {
        tasksSearchHelper.processedTaskInfo
    }

    private val taskTypesInfo: MutableLiveData<List<ITaskTypeInfo>> = MutableLiveData(emptyList())
    private val processedTask: MutableLiveData<ITask> = MutableLiveData()

    val taskTypeNames: MutableLiveData<List<String>> = taskTypesInfo.map { it?.map { type -> type.taskName } }
    val selectedTaskTypePosition: MutableLiveData<Int> = MutableLiveData(0)
    private val selectedTaskTypeInfo: MutableLiveData<ITaskTypeInfo> = selectedTaskTypePosition.map { getSelectedTypeTask() }
    val enabledChangeTaskType: MutableLiveData<Boolean> = processedTask.map { it == null && taskFromTaskList == null }
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

    val comment = selectedTaskTypeInfo.map { taskFromTaskList?.comment ?: getComment(it) }

    val enabledNextButton = selectedTaskTypeInfo.map { it != null && it.taskType != AppTaskTypes.Empty.taskType }

    val onClickTaskTypes = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            selectedTaskTypePosition.value = position
        }
    }

    init {
        viewModelScope.launch {
            taskTypesInfo.value = generalRepo.getTasksTypes()
            isStrictList.value = isStrictList()
            updateProcessedTask()
        }
    }

    fun getMarket(): String {
        return sessionInfo.market!!
    }

    fun onClickNext() {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()
            when (getSelectedTypeTask()?.taskType) {
                AppTaskTypes.CheckPrice.taskType -> {
                    if (taskFromTaskList != null) {
                        checkPriceTaskInfoNetRequest(
                                CheckPriceTaskInfoParams(
                                        taskNumber = taskFromTaskList!!.taskId,
                                        ip = deviceIndo.getDeviceIp(),
                                        withProductInfo = true.toSapBooleanString(),
                                        mode = "1"
                                )
                        )
                    } else {
                        Either.Right(null)
                    }.let {
                        it.either({ failure ->
                            screenNavigator.openAlertScreen(failure)
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
                            screenNavigator.openGoodsListPcScreen()
                        }

                    }

                }
                AppTaskTypes.CheckList.taskType -> {
                    newTask(
                            taskManager = checkListTaskManager,
                            taskDescription = CheckListTaskDescription(
                                    tkNumber = sessionInfo.market!!,
                                    taskNumber = taskFromTaskList?.taskId
                                            ?: "",
                                    taskName = taskName.value ?: "",
                                    comment = comment.value ?: "",
                                    description = description.value ?: "",
                                    isStrictList = taskFromTaskList?.isStrict ?: false
                            )
                    )
                    screenNavigator.openGoodsListClScreen()
                }
                AppTaskTypes.WorkList.taskType -> {
                    newTask(
                            taskManager = workListTaskManager,
                            taskDescription = WorkListTaskDescription(
                                    tkNumber = sessionInfo.market!!,
                                    taskNumber = taskFromTaskList?.taskId
                                            ?: "",
                                    taskName = taskName.value ?: "",
                                    comment = comment.value ?: "",
                                    description = description.value ?: "",
                                    isStrictList = taskFromTaskList?.isStrict ?: false
                            )
                    )
                    screenNavigator.openGoodsListWlScreen()
                }
                AppTaskTypes.NotExposedProducts.taskType -> {
                    if (taskFromTaskList != null) {
                        notExposedTaskInfoNetRequest(
                                NotExposedTaskInfoParams(
                                        taskNumber = taskFromTaskList!!.taskId,
                                        ip = deviceIndo.getDeviceIp(),
                                        withProductInfo = true.toSapBooleanString(),
                                        mode = "1"
                                )
                        )
                    } else {
                        Either.Right(null)
                    }.let {

                        it.either({ failure ->
                            screenNavigator.openAlertScreen(failure)
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
                            screenNavigator.openGoodsListNeScreen()
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
            return true
        }

        screenNavigator.openConfirmationExitTask(generalTaskManager.getProcessedTask()?.getDescription()?.taskName
                ?: "") {
            generalTaskManager.clearCurrentTask()
            screenNavigator.goBack()
        }

        return false
    }


    private fun getComment(taskTypeInfo: ITaskTypeInfo?): String {
        return taskTypeInfo?.annotation ?: ""
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
