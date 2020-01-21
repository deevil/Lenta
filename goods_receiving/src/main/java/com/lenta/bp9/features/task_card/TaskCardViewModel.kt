package com.lenta.bp9.features.task_card

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.change_datetime.ChangeDateTimeMode
import com.lenta.bp9.features.loading.tasks.TaskCardMode
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.NotificationIndicatorType
import com.lenta.bp9.model.task.TaskStatus
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.date_time.DateTimeUtil
import com.lenta.shared.utilities.extentions.map
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

    val taskType: TaskType by lazy {
        taskManager.getReceivingTask()?.taskHeader?.taskType ?: TaskType.None
    }

    val tvDeliveryCaption: String by lazy {
        when (taskManager.getReceivingTask()?.taskHeader?.taskType) {
            TaskType.DirectSupplier -> context.getString(R.string.incoming_delivery)
            TaskType.ReceptionDistributionCenter, TaskType.RecalculationCargoUnit -> context.getString(R.string.transportation)
            else -> context.getString(R.string.incoming_delivery)
        }
    }

    val tvCountCaption: String by lazy {
        when (taskManager.getReceivingTask()?.taskHeader?.taskType) {
            TaskType.DirectSupplier, TaskType.RecalculationCargoUnit -> context.getString(R.string.count_SKU)
            TaskType.ReceptionDistributionCenter -> context.getString(R.string.count_GE)
            else -> context.getString(R.string.count_SKU)
        }
    }

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

    val enabledBtn by lazy {
        notifications.map { notifications ->
            notifications!!.findLast { it.indicator == NotificationIndicatorType.Red } == null
        }
    }

    val bookmarkIndicator by lazy {
        val redIndicatorAttend = notifications.value?.findLast { it.indicator == NotificationIndicatorType.Red } != null
        val yellowIndicatorAttend= notifications.value?.findLast { it.indicator == NotificationIndicatorType.Yellow } != null
        when {
            redIndicatorAttend -> NotificationIndicatorType.Red
            yellowIndicatorAttend -> NotificationIndicatorType.Yellow
            else -> NotificationIndicatorType.None
        }
    }

    val visibilityBtn by lazy {
        MutableLiveData(taskManager.getReceivingTask()?.taskDescription?.currentStatus.let {
            it == TaskStatus.Checked || it == TaskStatus.Recounted || (it == TaskStatus.Unloaded && (taskType == TaskType.RecalculationCargoUnit || taskType == TaskType.ReceptionDistributionCenter) )
        })
    }

    val currentStatus by lazy {
        MutableLiveData(taskManager.getReceivingTask()?.taskDescription?.currentStatus)
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

    val currentStatusText by lazy {
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

    val isRecount by lazy {
        taskManager.getReceivingTask()?.taskDescription?.isRecount ?: false
    }

    val changeCurrentDateTimePossible by lazy {
        val status = taskManager.getReceivingTask()?.taskDescription?.currentStatus
        if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.ReceptionDistributionCenter && status == TaskStatus.Arrived) {
            false
        } else {
            status == TaskStatus.Arrived
        }
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

    fun onClickSecondButton() {
        when (taskType) {
            TaskType.RecalculationCargoUnit -> screenNavigator.openSkipRecountScreen()
            TaskType.ReceptionDistributionCenter -> screenNavigator.openTransportMarriageScreen()
            else -> {
                when (currentStatus.value) {
                    TaskStatus.Checked -> screenNavigator.openStartReviseLoadingScreen()
                    TaskStatus.Recounted -> screenNavigator.openRecountStartLoadingScreen()
                }
            }
        }
    }

    fun onClickSupply() {
        screenNavigator.openFormedDocsScreen()
    }

    fun onClickNext() {
        if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.RecalculationCargoUnit) {
            if (taskManager.getReceivingTask()?.taskHeader?.isCracked == false &&  taskManager.getReceivingTask()?.taskDescription?.isRecount == false) {
                //todo аналитик должен дописать алгоритм, карточка 2680
                return
            }

            if (taskManager.getReceivingTask()?.taskHeader?.isCracked == true &&  taskManager.getReceivingTask()?.taskDescription?.isRecount == false) {
                //todo Не пересчётная ГЕ с признаком "взлом", узнать у аналитика как сменить статус на "Взлом", карточка 2680
                return
            }

            if ( (taskManager.getReceivingTask()?.taskHeader?.isCracked == true &&  taskManager.getReceivingTask()?.taskDescription?.isRecount == true) ||
                    (taskManager.getReceivingTask()?.taskHeader?.isCracked == false &&  taskManager.getReceivingTask()?.taskDescription?.isRecount == true) ) {
                screenNavigator.openRecountStartPGELoadingScreen()
                return
            }

            return
        }

        when (taskManager.getReceivingTask()?.taskDescription?.currentStatus) {
            TaskStatus.Ordered, TaskStatus.Traveling, TaskStatus.TemporaryRejected -> screenNavigator.openRegisterArrivalLoadingScreen()
            TaskStatus.Arrived -> {
                if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.ReceptionDistributionCenter) {
                    screenNavigator.openUnloadingStartRDSLoadingScreen()
                } else {
                    screenNavigator.openStartReviseLoadingScreen()
                }
            }
            TaskStatus.Checked -> screenNavigator.openStartConditionsReviseLoadingScreen()
            TaskStatus.Unloaded -> {
                if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.ReceptionDistributionCenter) {
                    screenNavigator.openInputOutgoingFillingsScreen()
                } else {
                    screenNavigator.openRecountStartLoadingScreen()
                }
            }
            TaskStatus.Recounted -> screenNavigator.openTransmittedLoadingScreen()
            TaskStatus.Departure -> screenNavigator.openStartReviseLoadingScreen()
            TaskStatus.Booked -> screenNavigator.openTransferGoodsSectionScreen()
            TaskStatus.Completed -> screenNavigator.openFormedDocsScreen()
        }
    }

    fun onBackPressed() {
        if (mode == TaskCardMode.Full) {
            screenNavigator.openUnlockTaskLoadingScreen()
        } else {
            screenNavigator.goBack()
        }

    }

    data class NotificationVM(
            val number: String,
            val text: String,
            val indicator: NotificationIndicatorType
    )
}
