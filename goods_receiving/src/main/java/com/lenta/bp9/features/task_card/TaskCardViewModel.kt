package com.lenta.bp9.features.task_card

import android.content.Context
import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.change_datetime.ChangeDateTimeMode
import com.lenta.bp9.features.loading.tasks.TaskCardMode
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.NotificationIndicatorType
import com.lenta.bp9.model.task.TaskNotification
import com.lenta.bp9.model.task.TaskStatus
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.date_time.DateTimeUtil
import com.lenta.shared.utilities.extentions.map
import io.fabric.sdk.android.services.concurrency.Task
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskCardViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var timeMonitor: ITimeMonitor

    val selectedPage = MutableLiveData(0)

    var mode: TaskCardMode = TaskCardMode.None

    val taskCaption: String by lazy {
        taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    val notifications by lazy {
        MutableLiveData((taskManager.getReceivingTask()?.taskRepository?.getNotifications()?.getGeneralNotifications() ?: emptyList()).mapIndexed { index, notification ->
            NotificationVM(number = (index + 1).toString(),
                    text = notification.text,
                    indicator = notification.indicator)
        })
    }

    val redIndicatorAbsent by lazy {
        notifications.map { notifications ->
            notifications!!.findLast { it.indicator == NotificationIndicatorType.Red } == null
        }
    }

    val numberTTN by lazy {
        taskManager.getReceivingTask()?.taskDescription?.ttnNumber ?: ""
    }

    val order by lazy {
        taskManager.getReceivingTask()?.taskDescription?.orderNumber ?: ""
    }

    val incomingDelivery by lazy {
        taskManager.getReceivingTask()?.taskDescription?.deliveryNumber ?: ""
    }

    val arrival by lazy {
        taskManager.getReceivingTask()?.taskDescription?.apply {
            return@lazy this.plannedDeliveryDate + " // " + this.plannedDeliveryTime + "\n" +
                    this.actualArrivalDate + " // " + this.actualArrivalTime
        }
    }

    val countSKU by lazy {
        taskManager.getReceivingTask()?.taskDescription?.quantityPositions.toString()
    }

    val currentStatus by lazy {
        taskManager.getReceivingTask()?.taskDescription?.currentStatusText ?: ""
    }

    val nextStatus by lazy {
        taskManager.getReceivingTask()?.taskDescription?.nextStatusText ?: ""
    }

    val isPromo by lazy {
        taskManager.getReceivingTask()?.taskDescription?.isPromo ?: false
    }

    val isAlco by lazy {
        taskManager.getReceivingTask()?.taskDescription?.isAlco ?: false
    }

    val isFruit by lazy {
        taskManager.getReceivingTask()?.taskDescription?.isUFF ?: false
    }

    val isRaw by lazy {
        taskManager.getReceivingTask()?.taskDescription?.isRawMaterials ?: false
    }

    val isReturn by lazy {
        taskManager.getReceivingTask()?.taskDescription?.isSupplierReturnAvailability ?: false
    }

    val isNotEDI by lazy {
        taskManager.getReceivingTask()?.taskDescription?.isNotEDI ?: false
    }

    val isSpecial by lazy {
        taskManager.getReceivingTask()?.taskDescription?.isSpecialControlGoods ?: false
    }

    val changeCurrentDateTimePossible by lazy {
        val status = taskManager.getReceivingTask()?.taskDescription?.currentStatus
        status == TaskStatus.Arrived
    }

    val changeNextDateTimePossible by lazy {
        val status = taskManager.getReceivingTask()?.taskDescription?.currentStatus
        status == TaskStatus.Ordered || status == TaskStatus.Traveling || status == TaskStatus.Arrived ||
                status == TaskStatus.Checked
    }

    val currentStatusDateTime: MutableLiveData<String> = MutableLiveData("")
    val nextStatusDateTime: MutableLiveData<String> = MutableLiveData("")

    init {
        viewModelScope.launch {
            val timeInMillis = timeMonitor.getUnixTime()
            taskManager.getReceivingTask()?.taskDescription?.nextStatusDate = DateTimeUtil.formatDate(timeInMillis, Constants.DATE_FORMAT_yyyy_mm_dd)
            taskManager.getReceivingTask()?.taskDescription?.nextStatusTime = DateTimeUtil.formatDate(timeInMillis, Constants.TIME_FORMAT_hhmmss)
        }
    }

    fun onResume() {
        viewModelScope.launch {
            updateDateTimes()
        }
    }

    private fun updateDateTimes() {
        val currentDate = taskManager.getReceivingTask()?.taskDescription?.currentStatusDate ?: ""
        val currentTime = taskManager.getReceivingTask()?.taskDescription?.currentStatusTime ?: ""
        val nextDate = taskManager.getReceivingTask()?.taskDescription?.nextStatusDate ?: ""
        val nextTime = taskManager.getReceivingTask()?.taskDescription?.nextStatusTime ?: ""
        currentStatusDateTime.value = currentDate + "\n" + currentTime
        nextStatusDateTime.value = nextDate + "\n" + nextTime
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun changeCurrentDatetimeTapped() {
        screenNavigator.openChangeDateTimeScreen(ChangeDateTimeMode.CurrentStatus)
    }

    fun changeNextDatetimeTapped() {
        screenNavigator.openChangeDateTimeScreen(ChangeDateTimeMode.NextStatus)
    }

    fun onClickNext() {
        when (taskManager.getReceivingTask()?.taskDescription?.currentStatus) {
            TaskStatus.Ordered, TaskStatus.Traveling -> {
                screenNavigator.openLoadingRegisterArrivalScreen()
            }
            TaskStatus.Arrived -> {
                screenNavigator.openStartReviseLoadingScreen()
            }
            TaskStatus.Checked -> {
                screenNavigator.openStartConditionsReviseLoadingScreen()
            }
            TaskStatus.Unloaded -> {
                screenNavigator.openRecountStartLoadingScreen()
            }
        }
    }

    fun onBackPressed() {
        screenNavigator.openUnlockTaskLoadingScreen()
    }

    data class NotificationVM(
            val number: String,
            val text: String,
            val indicator: NotificationIndicatorType
    )
}
