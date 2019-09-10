package com.lenta.bp14.features.job_card

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.check_price.CheckPriceTaskDescription
import com.lenta.bp14.models.check_price.CheckPriceTaskManager
import com.lenta.bp14.models.general.IGeneralRepo
import com.lenta.bp14.models.general.ITaskType
import com.lenta.bp14.models.general.TaskTypes
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
    lateinit var checkPriceTaskManager: CheckPriceTaskManager

    private lateinit var taskNumber: String

    private var taskTypes: MutableLiveData<List<ITaskType>> = MutableLiveData()

    var taskTypeNames: MutableLiveData<List<String>> = taskTypes.map { it?.map { type -> type.taskName } }
    val selectedTaskTypePosition: MutableLiveData<Int> = MutableLiveData(0)
    val enabledChangeTaskType: MutableLiveData<Boolean> = MutableLiveData(true)
    val taskName = MutableLiveData("taskName")
    val description = MutableLiveData("Содержание описания")
    val comment = MutableLiveData("Содержание комментария")

    init {
        viewModelScope.launch {
            taskTypes.value = generalRepo.getTasksTypes()
        }
    }

    fun getMarket(): String {
        return sessionInfo.market!!
    }

    fun setTaskNumber(taskNumber: String) {
        this.taskNumber = taskNumber
    }

    fun onClickNext() {
        when (getSelectedTypeTask()) {
            TaskTypes.CheckPrice.taskType -> newCheckPriceTask()
            TaskTypes.CheckList.taskType -> screenNavigator.openGoodsListClScreen()
            else -> screenNavigator.openNotImplementedScreenAlert("")
        }
    }

    private fun newCheckPriceTask() {
        checkPriceTaskManager.clearTask()
        checkPriceTaskManager.newTask(
                taskDescription = CheckPriceTaskDescription(
                        tkNumber = sessionInfo.market!!,
                        taskName = taskName.value ?: ""

                )
        )
        screenNavigator.openGoodsListPcScreen()
    }

    private fun getSelectedTypeTask(): ITaskType? {
        return selectedTaskTypePosition.value?.let {
            taskTypes.value?.getOrNull(it)
        }
    }

    val onClickTaskTypes = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            selectedTaskTypePosition.value = position
        }
    }


}
