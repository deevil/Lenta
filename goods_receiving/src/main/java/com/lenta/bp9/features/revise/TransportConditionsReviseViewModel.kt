package com.lenta.bp9.features.revise

import android.content.Context
import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.shared.R
import com.lenta.bp9.features.task_card.TaskCardViewModel
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.revise.ConditionType
import com.lenta.bp9.model.task.revise.ConditionViewType
import com.lenta.bp9.model.task.revise.TransportCondition
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class TransportConditionsReviseViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    val selectedPage = MutableLiveData(0)


    val taskCaption: String by lazy {
        taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    val notifications by lazy {
        val notificationsData = taskManager.getReceivingTask()?.taskRepository?.getNotifications()?.getReviseConditionsNotifications() ?: emptyList()
        MutableLiveData(notificationsData.mapIndexed { index, notification ->
            TaskCardViewModel.NotificationVM(number = (notificationsData.size - index).toString(),
                    text = notification.text,
                    indicator = notification.indicator)
        })
    }

    val conditionsToCheck: MutableLiveData<List<TransportConditionVM>> = MutableLiveData()
    val checkedConditions: MutableLiveData<List<TransportConditionVM>> = MutableLiveData()
    val saveEnabled = conditionsToCheck.map { conditions -> conditions?.findLast { it.isObligatory } == null }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickReject() {
        screenNavigator.openRejectScreen()
    }

    fun onClickNext() {
        screenNavigator.openFinishConditionsReviseLoadingScreen()
    }

    fun onResume() {
        updateVMs()
    }

    private fun updateVMs() {
        val checked = taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getTransportConditions()?.filter { it.isCheck } ?: emptyList()
        val unchecked = taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getTransportConditions()?.filter { !it.isCheck } ?: emptyList()

        conditionsToCheck.value = mapConditions(unchecked)
        checkedConditions.value = mapConditions(checked)

        viewModelScope.launch {
            moveToPreviousPageIfNeeded()
        }
    }

    private fun moveToPreviousPageIfNeeded() {
        if (selectedPage.value == 0) {
            selectedPage.value = if (conditionsToCheck.value?.size == 0 && checkedConditions.value?.size != 0) 1 else 0
        } else {
            selectedPage.value = if (checkedConditions.value?.size == 0) 0 else 1
        }
    }

    private fun mapConditions(conditions: List<TransportCondition>): List<TransportConditionVM> {
        return conditions.mapIndexed { index, condition ->
            TransportConditionVM(
                    position = (conditions.size - index).toString(),
                    name = condition.conditionName,
                    isChecked = condition.isCheck,
                    value = MutableLiveData(condition.value),
                    isObligatory = condition.isObligatory,
                    conditionType = condition.conditionType,
                    conditionViewType = condition.conditionViewType,
                    id = condition.conditionID,
                    suffix = if (condition.conditionViewType == ConditionViewType.Temperature) MutableLiveData(context.getString(R.string.celsius)) else MutableLiveData("")
            )
        }
    }

    fun checkedChanged(position: Int, checked: Boolean) {
        val conditionID = if (checked) conditionsToCheck.value?.get(position)?.id else checkedConditions.value?.get(position)?.id
        conditionID?.let {
            taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.changeTransportConditionStatus(it)
            updateVMs()
        }
    }

    fun finishedInput(position: Int) {
        val condition = conditionsToCheck.value?.get(position)
        condition?.let {
            taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.changeTransportConditionValue(it.id, it.value.value ?: "")
            updateVMs()
        }
    }

    fun onBackPressed() {
        screenNavigator.openUnlockTaskLoadingScreen()
    }
}

data class TransportConditionVM(
        val position: String,
        val name: String,
        val isChecked: Boolean,
        val value: MutableLiveData<String>,
        val isObligatory: Boolean,
        val conditionType: ConditionType,
        val conditionViewType: ConditionViewType,
        val suffix: MutableLiveData<String>,

        val id: String
)