package com.lenta.bp9.features.task_card

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.change_datetime.ChangeDateTimeMode
import com.lenta.bp9.features.loading.tasks.TaskCardMode
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.date_time.DateTimeUtil
import com.lenta.shared.utilities.extentions.getDeviceIp
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
    @Inject
    lateinit var zmpUtzGrz39V001NetRequest: ZmpUtzGrz39V001NetRequest

    val selectedPage = MutableLiveData(0)

    var mode: TaskCardMode = TaskCardMode.None

    val taskType: TaskType by lazy {
        taskManager.getReceivingTask()?.taskHeader?.taskType ?: TaskType.None
    }

    val tvDeliveryCaption: String by lazy {
        when (taskManager.getReceivingTask()?.taskHeader?.taskType) {
            TaskType.DirectSupplier -> context.getString(R.string.incoming_delivery)
            TaskType.ReceptionDistributionCenter, TaskType.OwnProduction, TaskType.RecalculationCargoUnit -> context.getString(R.string.transportation)
            else -> context.getString(R.string.incoming_delivery)
        }
    }

    val tvCountCaption: String by lazy {
        when (taskManager.getReceivingTask()?.taskHeader?.taskType) {
            TaskType.DirectSupplier, TaskType.RecalculationCargoUnit -> context.getString(R.string.count_SKU)
            TaskType.ReceptionDistributionCenter -> context.getString(R.string.count_GE)
            TaskType.OwnProduction -> context.getString(R.string.count_EO)
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
        MutableLiveData(if (taskType == TaskType.ShipmentRC && taskManager.getReceivingTask()?.taskDescription?.currentStatus == TaskStatus.Ordered) {
            false
        } else {
            notifications.value?.findLast {
                it.indicator == NotificationIndicatorType.Red
            } == null
        })
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
            it == TaskStatus.Checked || it == TaskStatus.Recounted ||
                    (it == TaskStatus.Unloaded && (taskType == TaskType.RecalculationCargoUnit || taskType == TaskType.ReceptionDistributionCenter || taskType == TaskType.OwnProduction)) ||
                    (it == TaskStatus.ReadyToShipment && taskType == TaskType.ShipmentRC )
        })
    }

    val visibilityNextBtn by lazy {
        MutableLiveData(taskManager.getReceivingTask()?.taskDescription?.currentStatus.let {
            !(taskType == TaskType.ShipmentRC && (it == TaskStatus.ShipmentSentToGis || it == TaskStatus.ShipmentRejectedByGis || it == TaskStatus.Departure)  )
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

    val isVet by lazy {
        taskManager.getReceivingTask()?.taskDescription?.isVet ?: false
    }

    val changeCurrentDateTimePossible by lazy {
        val status = taskManager.getReceivingTask()?.taskDescription?.currentStatus
        if ((taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.ReceptionDistributionCenter || taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.OwnProduction) &&
                status == TaskStatus.Arrived) {
            false
        } else {
            status == TaskStatus.Arrived
        }
    }

    val changeNextDateTimePossible by lazy {
        val status = taskManager.getReceivingTask()?.taskDescription?.currentStatus
        if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.ShipmentRC) {
            status == TaskStatus.Traveling || status == TaskStatus.Arrived
        } else {
            status == TaskStatus.Ordered || status == TaskStatus.Traveling || status == TaskStatus.Arrived || status == TaskStatus.Checked
        }
    }

    val currentStatusDateTime: MutableLiveData<String> = MutableLiveData("")
    val nextStatusDateTime: MutableLiveData<String> = MutableLiveData("")

    val shipmentNumberTN by lazy {
        taskManager.getReceivingTask()?.taskDescription?.ttnNumber ?: ""
    }
    val shipmentNumberTTN by lazy {
        taskManager.getReceivingTask()?.taskDescription?.deliveryNumber ?: ""
    }
    val shipmentPlanDate by lazy {
        taskManager.getReceivingTask()?.taskDescription?.plannedDeliveryDate ?: ""
    }
    val shipmentFactDate by lazy {
        taskManager.getReceivingTask()?.taskDescription?.actualArrivalDate ?: ""
    }
    val shipmentOrder by lazy {
        taskManager.getReceivingTask()?.taskDescription?.orderNumber ?: ""
    }
    val shipmentDelivery by lazy {
        taskManager.getReceivingTask()?.taskDescription?.deliveryNumber ?: ""
    }
    val shipmentDeliveryOTM by lazy {
        taskManager.getReceivingTask()?.taskDescription?.deliveryNumberOTM ?: ""
    }
    val shipmentTransportation by lazy {
        taskManager.getReceivingTask()?.taskDescription?.transportationNumber ?: ""
    }
    val countGE by lazy {
        taskManager.getReceivingTask()?.taskDescription?.countGE ?: ""
    }

    val countEO by lazy {
        taskManager.getReceivingTask()?.taskDescription?.countEO ?: ""
    }


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
        when (currentStatus.value) {
            TaskStatus.Unloaded -> {
                when (taskType) {
                    TaskType.RecalculationCargoUnit -> screenNavigator.openSkipRecountScreen()
                    TaskType.ReceptionDistributionCenter, TaskType.OwnProduction -> screenNavigator.openTransportMarriageScreen()
                }
            }
            TaskStatus.Checked -> screenNavigator.openStartReviseLoadingScreen()
            TaskStatus.Recounted -> screenNavigator.openRecountStartLoadingScreen()
            TaskStatus.ReadyToShipment -> {
                if (taskType == TaskType.ShipmentRC) {
                    screenNavigator.openShipmentPurposeTransportLoadingScreen("2", taskManager.getReceivingTask()?.taskDescription?.transportationNumber ?: "")
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

        if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.ShipmentRC) {
            when (currentStatus.value) {
                TaskStatus.ReadyToShipment -> screenNavigator.openTransportationNumberScreen()
                TaskStatus.Traveling -> screenNavigator.openDriverDataScreen()
                TaskStatus.Arrived -> screenNavigator.openShipmentStartLoadingScreen(taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber ?: "")
                TaskStatus.ConditionsTested -> shipmentStartRecount()
                TaskStatus.Recounted -> {
                    if (taskManager.getReceivingTask()?.taskDescription?.submergedGE?.isNotEmpty() == true) {
                        screenNavigator.openShipmentAdjustmentConfirmationDialog(
                                submergedGE = taskManager.getReceivingTask()?.taskDescription?.submergedGE ?: "",
                                nextCallbackFunc = {
                                    screenNavigator.openShipmentPostingLoadingScreen()
                                }
                        )
                    } else {
                        screenNavigator.openShipmentPostingLoadingScreen()
                    }
                }
                TaskStatus.ShipmentAllowedByGis -> return //todo Разрешено ГИС. Отгрузка, аналитик не описал условие в документации
                TaskStatus.Loaded -> screenNavigator.openInputOutgoingFillingsScreen()
            }
            return
        }

        when (taskManager.getReceivingTask()?.taskDescription?.currentStatus) {
            TaskStatus.Ordered, TaskStatus.Traveling, TaskStatus.TemporaryRejected -> screenNavigator.openRegisterArrivalLoadingScreen()
            TaskStatus.Arrived -> {
                if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.ReceptionDistributionCenter || taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.OwnProduction) {
                    screenNavigator.openUnloadingStartRDSLoadingScreen()
                } else {
                    screenNavigator.openStartReviseLoadingScreen()
                }
            }
            TaskStatus.Checked -> screenNavigator.openStartConditionsReviseLoadingScreen()
            TaskStatus.Unloaded -> {
                if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.ReceptionDistributionCenter || taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.OwnProduction) {
                    screenNavigator.openNoTransportDefectDeclaredDialog(
                            nextCallbackFunc = {
                                screenNavigator.openInputOutgoingFillingsScreen()
                            }
                    )
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

    private fun shipmentStartRecount() {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()
            val params = ZmpUtzGrz39V001Params(
                    taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber ?: "",
                    deviceIP = context.getDeviceIp(),
                    personalNumber = sessionInfo.personnelNumber ?: "",
                    recountStartDate = taskManager.getReceivingTask()?.taskDescription?.nextStatusDate ?: "",
                    recountStartTime = taskManager.getReceivingTask()?.taskDescription?.nextStatusTime ?: ""
            )
            zmpUtzGrz39V001NetRequest(params).either(::handleFailure, ::handleSuccess)
            screenNavigator.hideProgress()
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleSuccess(result: ZmpUtzGrz39V001Result) {
        val cargoUnits = result.cargoUnits.map { TaskCargoUnitInfo.from(it) }
        taskManager.getReceivingTask()?.taskRepository?.getCargoUnits()?.updateCargoUnits(cargoUnits)

        screenNavigator.openControlDeliveryCargoUnitsScreen()
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
