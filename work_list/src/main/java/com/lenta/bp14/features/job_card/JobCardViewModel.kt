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
import com.lenta.bp14.models.general.IGeneralRepo
import com.lenta.bp14.models.general.ITaskTypeInfo
import com.lenta.bp14.models.general.AppTaskTypes
import com.lenta.bp14.models.not_exposed_products.NotExposedProductsTaskDescription
import com.lenta.bp14.models.not_exposed_products.NotExposedProductsTaskManager
import com.lenta.bp14.models.work_list.WorkListTaskDescription
import com.lenta.bp14.models.work_list.WorkListTaskManager
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
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


    private lateinit var taskNumber: String

    private val taskTypesInfo: MutableLiveData<List<ITaskTypeInfo>> = MutableLiveData(listOf())
    private val processedTask: MutableLiveData<ITask> = MutableLiveData()

    val taskTypeNames: MutableLiveData<List<String>> = taskTypesInfo.map { it?.map { type -> type.taskName } }
    val selectedTaskTypePosition: MutableLiveData<Int> = MutableLiveData(0)
    private val selectedTaskTypeInfo: MutableLiveData<ITaskTypeInfo> = selectedTaskTypePosition.map { getSelectedTypeTask() }
    val enabledChangeTaskType: MutableLiveData<Boolean> = processedTask.map { it == null }
    val isStrictList = MutableLiveData(false)

    val taskName by lazy {
        selectedTaskTypeInfo.map { selectedTaskType ->
            processedTask.value.let { task ->
                task?.getDescription()?.taskName
                        ?: generalTaskManager.generateNewNameForTask(selectedTaskType)
            }
        }
    }

    val description = selectedTaskTypeInfo.map { it?.annotation }

    val comment = selectedTaskTypeInfo.map { getComment(it) }

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

    fun setTaskNumber(taskNumber: String) {
        this.taskNumber = taskNumber
    }

    fun onClickNext() {
        when (getSelectedTypeTask()?.taskType) {
            AppTaskTypes.CheckPrice.taskType -> {
                newTask(
                        taskManager = checkPriceTaskManager,
                        taskDescription = CheckPriceTaskDescription(
                                tkNumber = sessionInfo.market!!,
                                taskNumber = taskNumber,
                                taskName = taskName.value ?: "",
                                comment = comment.value ?: "",
                                description = description.value ?: ""
                        )
                )
                screenNavigator.openGoodsListPcScreen()
            }
            AppTaskTypes.CheckList.taskType -> {
                newTask(
                        taskManager = checkListTaskManager,
                        taskDescription = CheckListTaskDescription(
                                tkNumber = sessionInfo.market!!,
                                taskNumber = taskNumber,
                                taskName = taskName.value ?: "",
                                comment = comment.value ?: "",
                                description = description.value ?: ""
                        )
                )
                screenNavigator.openGoodsListClScreen()
            }
            AppTaskTypes.WorkList.taskType -> {
                newTask(
                        taskManager = workListTaskManager,
                        taskDescription = WorkListTaskDescription(
                                tkNumber = sessionInfo.market!!,
                                taskNumber = taskNumber,
                                taskName = taskName.value ?: "",
                                comment = comment.value ?: "",
                                description = description.value ?: ""
                        )
                )
                screenNavigator.openGoodsListWlScreen()
            }
            AppTaskTypes.NotExposedProducts.taskType -> {
                newTask(
                        taskManager = notExposedProductsTaskManager,
                        taskDescription = NotExposedProductsTaskDescription(
                                tkNumber = sessionInfo.market!!,
                                taskNumber = taskNumber,
                                taskName = taskName.value ?: "",
                                comment = comment.value ?: "",
                                description = description.value ?: ""
                        )
                )
                screenNavigator.openGoodsListNeScreen()
            }
            else -> screenNavigator.openNotImplementedScreenAlert("")
        }
    }

    fun onBackPressed(): Boolean {
        if (generalTaskManager.getProcessedTaskType() == null) {
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
        //TODO уточнить для ветки работы с заданиями
        return taskNumber.isNotBlank()
    }

    private fun updateProcessedTask() {
        generalTaskManager.getProcessedTask().let { processedTask ->
            this.processedTask.value = generalTaskManager.getProcessedTask()

            if (processedTask != null) {
                taskTypesInfo.value?.apply {
                    for (pos in 0..this.size) {
                        if (processedTask.getTaskType() == this[pos]) {
                            selectedTaskTypePosition.value = pos
                            return
                        }
                    }
                }
            }
        }
    }

}
