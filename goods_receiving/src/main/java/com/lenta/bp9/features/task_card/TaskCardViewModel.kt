package com.lenta.bp9.features.task_card

import android.content.Context
import androidx.lifecycle.MutableLiveData
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
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.mobrun.plugin.api.HyperHive
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

    @Inject
    lateinit var fixationDepartureReceptionDistrCenterNetRequest: FixationDepartureReceptionDistrCenterNetRequest

    @Inject
    lateinit var skipRecountNetRequest: SkipRecountNetRequest

    @Inject
    lateinit var zmpUtzGrz41V001NetRequest: ZmpUtzGrz41V001NetRequest

    @Inject
    lateinit var hyperHive: HyperHive


    var mode: TaskCardMode = TaskCardMode.None

    val taskType: TaskType by lazy {
        taskManager.getTaskType()
    }

    val tvDeliveryCaption: String by lazy {
        when (taskType) {
            TaskType.DirectSupplier, TaskType.OwnProduction -> context.getString(R.string.incoming_delivery)
            TaskType.ReceptionDistributionCenter, TaskType.RecalculationCargoUnit, TaskType.ShoppingMall -> context.getString(R.string.transportation)
            else -> context.getString(R.string.incoming_delivery)
        }
    }

    val tvCountCaption: String by lazy {
        when (taskType) {
            TaskType.DirectSupplier, TaskType.RecalculationCargoUnit -> context.getString(R.string.count_SKU)
            TaskType.ReceptionDistributionCenter, TaskType.ShoppingMall -> context.getString(R.string.count_GE)
            TaskType.OwnProduction -> context.getString(R.string.count_EO)
            else -> context.getString(R.string.count_SKU)
        }
    }

    val taskCaption: String by lazy {
        taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    val notifications by lazy {
        MutableLiveData((taskManager.getReceivingTask()?.taskRepository?.getNotifications()?.getGeneralNotifications()
                ?: emptyList()).mapIndexed { index, notification ->
            NotificationVM(number = (index + 1).toString(),
                    text = notification.text,
                    indicator = notification.indicator)
        })
    }

    val enabledBtn by lazy {
        MutableLiveData(if ((taskType == TaskType.ShipmentRC && taskManager.getReceivingTask()?.taskDescription?.currentStatus == TaskStatus.Ordered) || (taskManager.getReceivingTask()?.taskDescription?.currentStatus == TaskStatus.SentToGIS)) {
            false
        } else {
            notifications.value?.findLast {
                it.indicator == NotificationIndicatorType.Red
            } == null
        })
    }

    val bookmarkIndicator by lazy {
        val redIndicatorAttend = notifications.value?.findLast { it.indicator == NotificationIndicatorType.Red } != null
        val yellowIndicatorAttend = notifications.value?.findLast { it.indicator == NotificationIndicatorType.Yellow } != null
        when {
            redIndicatorAttend -> NotificationIndicatorType.Red
            yellowIndicatorAttend -> NotificationIndicatorType.Yellow
            else -> NotificationIndicatorType.None
        }
    }

    private val isShipmentPPSkipRecount by lazy {
        taskManager.getReceivingTask()?.taskDescription?.currentStatus == TaskStatus.Checked &&
                taskType == TaskType.ShipmentPP &&
                taskManager.getReceivingTask()?.taskDescription?.isSkipCountMan == true
    }

    val isSecondBtnVisible by lazy {
        MutableLiveData(getIsCurrentStatusAvailable())
    }

    ///---BEGIN THIS IS FOR SECOND BUTTON ---///
    private fun getIsCurrentStatusAvailable(): Boolean {
        val curStat = taskManager.getReceivingTask()?.taskDescription?.currentStatus
        return curStat == TaskStatus.Recounted
                || (curStat == TaskStatus.Checked && taskType != TaskType.ShipmentPP)
                || (curStat == TaskStatus.Unloaded && isTaskTypeForUnloaded())
                || (curStat == TaskStatus.ReadyToShipment && taskType == TaskType.ShipmentRC)
    }

    private fun isTaskTypeForUnloaded(): Boolean {
        return (taskType == TaskType.RecalculationCargoUnit || taskType == TaskType.ReceptionDistributionCenter || taskType == TaskType.ShoppingMall)
    }
    ////---END THIS IS FOR SECOND BUTTON ---/////

    val visibilityBtnFourth by lazy {
        MutableLiveData(isShipmentPPSkipRecount)
    }

    val visibilityNextBtn by lazy {
        MutableLiveData(taskManager.getReceivingTask()?.taskDescription?.currentStatus.let {
            if (taskType == TaskType.ShipmentRC && (it == TaskStatus.ShipmentSentToGis || it == TaskStatus.ShipmentRejectedByGis || it == TaskStatus.Departure)) {
                false
            } else {
                it != TaskStatus.Completed
            }
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

    //trello https://trello.com/c/XKpOCvZo
    val isEdo by lazy {
        taskManager.getReceivingTask()?.taskDescription?.isEDO == true
    }

    //https://trello.com/c/74l1kXcn
    val isMarkingProduct by lazy {
        taskManager.getReceivingTask()?.taskDescription?.isMark == true
    }

    //для ОПП, п.5.5.3 из ТП
    private val isBksDiff by lazy {
        taskManager.getReceivingTask()?.taskDescription?.isBksDiff == true
    }

    val isBksTN by lazy { //https://trello.com/c/VQOineBU
        taskManager.getReceivingTask()?.taskDescription?.isBksTN == true
    }

    val changeCurrentDateTimePossible by lazy {
        val status = taskManager.getReceivingTask()?.taskDescription?.currentStatus
        if (getIsTaskTypeForStatusArrived() && status == TaskStatus.Arrived) {
            false
        } else {
            status == TaskStatus.Arrived
        }
    }

    private fun getIsTaskTypeForStatusArrived(): Boolean {
        return taskType == TaskType.ReceptionDistributionCenter || taskType == TaskType.OwnProduction || taskType == TaskType.ShoppingMall
    }

    val changeNextDateTimePossible by lazy {
        val status = taskManager.getReceivingTask()?.taskDescription?.currentStatus
        if (taskType == TaskType.ShipmentRC) {
            status == TaskStatus.Traveling || status == TaskStatus.Arrived
        } else {
            status == TaskStatus.Ordered || status == TaskStatus.Traveling || status == TaskStatus.Arrived || status == TaskStatus.Checked
        }
    }

    val currentStatusDateTime: MutableLiveData<String> = MutableLiveData("")
    val nextStatusDateTime: MutableLiveData<String> = MutableLiveData("")

    val shipmentNumberTN by lazy {
        taskManager.getReceivingTask()?.taskDescription?.tnNumber.orEmpty()
    }

    val shipmentNumberTTN by lazy {
        taskManager.getReceivingTask()?.taskDescription?.ttnNumber.orEmpty()
    }

    val shipmentPlanDate by lazy {
        taskManager.getReceivingTask()?.taskDescription?.plannedDeliveryDate.orEmpty()
    }

    val shipmentFactDate by lazy {
        taskManager.getReceivingTask()?.taskDescription?.actualArrivalDate.orEmpty()
    }

    val shipmentOrder by lazy {
        if (taskType == TaskType.ShipmentRC) {
            taskManager.getReceivingTask()?.taskDescription?.shipmentOrder.orEmpty()
        } else {
            taskManager.getReceivingTask()?.taskDescription?.orderNumber.orEmpty()
        }

    }

    val shipmentDelivery by lazy {
        if (taskType == TaskType.ShipmentRC) {
            taskManager.getReceivingTask()?.taskDescription?.shipmentDelivery.orEmpty()
        } else {
            taskManager.getReceivingTask()?.taskDescription?.deliveryNumber.orEmpty()
        }

    }

    val isTaskTypeShipmentRC by lazy { taskType == TaskType.ShipmentRC }

    val shipmentDeliveryOTM by lazy {
        taskManager.getReceivingTask()?.taskDescription?.deliveryNumberOTM.orEmpty()
    }

    val shipmentTransportation by lazy {
        taskManager.getReceivingTask()?.taskDescription?.transportationNumber.orEmpty()
    }

    val stringsGoods by lazy {
        taskManager.getReceivingTask()?.taskDescription?.quantityPositions.toString()
    }

    val quantityST by lazy {
        taskManager.getReceivingTask()?.taskDescription?.quantityST.toStringFormatted()
    }

    val quantityKG by lazy {
        taskManager.getReceivingTask()?.taskDescription?.quantityKG.toStringFormatted()
    }

    val quantityAll by lazy {
        taskManager.getReceivingTask()?.taskDescription?.quantityAll.toStringFormatted()
    }


    init {
        launchUITryCatch {
            val timeInMillis = timeMonitor.getUnixTime()
            taskManager.getReceivingTask()?.taskDescription?.nextStatusDate = DateTimeUtil.formatDate(timeInMillis, Constants.DATE_FORMAT_yyyy_mm_dd)
            taskManager.getReceivingTask()?.taskDescription?.nextStatusTime = DateTimeUtil.formatDate(timeInMillis, Constants.TIME_FORMAT_hhmmss)
        }
    }

    fun onResume() {
        launchUITryCatch {
            updateDateTimes()
        }
    }

    private fun updateDateTimes() {
        val currentDate = taskManager.getReceivingTask()?.taskDescription?.currentStatusDate ?: ""
        val currentTime = taskManager.getReceivingTask()?.taskDescription?.currentStatusTime ?: ""
        val nextDate = taskManager.getReceivingTask()?.taskDescription?.nextStatusDate ?: ""
        val nextTime = taskManager.getReceivingTask()?.taskDescription?.nextStatusTime ?: ""
        currentStatusDateTime.value = currentDate + "\n" + currentTime
        //для текущего статуса Выполнено не показываем дату и время для нового статуса, с сервера почему-то это поле приходит заполненным
        if (taskManager.getReceivingTask()?.taskDescription?.currentStatus != TaskStatus.Completed) {
            nextStatusDateTime.value = nextDate + "\n" + nextTime
        }
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
                    TaskType.ReceptionDistributionCenter, TaskType.ShoppingMall -> screenNavigator.openTransportMarriageScreen()
                    else -> Unit
                }
            }
            TaskStatus.Checked -> {
                screenNavigator.openStartReviseLoadingScreen()
            }
            TaskStatus.Recounted -> screenNavigator.openRecountStartLoadingScreen()
            TaskStatus.ReadyToShipment -> {
                if (taskType == TaskType.ShipmentRC) {
                    screenNavigator.openShipmentPurposeTransportLoadingScreen(
                            mode = "2",
                            transportationNumber = taskManager.getReceivingTask()?.taskDescription?.transportationNumber.orEmpty()
                    )
                }
            }
            else -> Unit
        }
    }

    fun onClickDocs() {
        screenNavigator.openFormedDocsScreen()
    }

    fun onClickFourth() {
        shipmentSkipRecount()
    }

    fun onClickNext() {
        when (taskManager.getTaskType()) {
            TaskType.RecalculationCargoUnit -> clickNextByTaskRecalculationCargoUnit()
            TaskType.ShipmentRC -> clickNextByTaskShipmentRC()
            TaskType.ShipmentPP -> clickNextByTaskShipmentPP()
            else -> clickNextByCurrentStatus()
        }
    }

    private fun clickNextByTaskRecalculationCargoUnit() {
        //карточка trello https://trello.com/c/BShSWFgU
        val isCracked =
                taskManager
                        .getReceivingTask()
                        ?.taskHeader
                        ?.isCracked

        val isRecount =
                taskManager
                        .getReceivingTask()
                        ?.taskDescription
                        ?.isRecount

        if (isCracked == false && isRecount == false) {
            //todo аналитик должен дописать алгоритм, карточка 2680
            return
        }

        if (isCracked == true && isRecount == false) {
            //todo Не пересчётная ГЕ с признаком "взлом", узнать у аналитика как сменить статус на "Взлом", карточка 2680
            return
        }

        if ((isCracked == true && isRecount == true)
                || (isCracked == false && isRecount == true)) {
            screenNavigator.openRecountStartPGELoadingScreen()
        }
    }

    private fun clickNextByTaskShipmentRC() {
        when (currentStatus.value) {
            TaskStatus.ReadyToShipment -> screenNavigator.openTransportationNumberScreen()
            TaskStatus.Traveling -> clickNextByTaskShipmentRCByStatusTraveling()
            TaskStatus.Arrived -> screenNavigator.openShipmentStartLoadingScreen(taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber.orEmpty())
            TaskStatus.ConditionsTested -> shipmentStartRecount()
            TaskStatus.Recounted -> clickNextByTaskShipmentRCByStatusRecounted()
            TaskStatus.ShipmentAllowedByGis -> shipmentAllowedByGis() // https://trello.com/c/FnABffRE
            TaskStatus.Loaded -> screenNavigator.openInputOutgoingFillingsScreen()
            else -> Unit
        }
    }

    private fun clickNextByTaskShipmentRCByStatusTraveling() {
        val isAlco = taskManager.getReceivingTask()?.taskDescription?.isAlco
        if (isAlco == true) {
            screenNavigator.openDriverDataScreen()
        } else {
            screenNavigator.openShipmentArrivalLockLoadingScreen(
                    TaskDriverDataInfo(
                            initials = "",
                            passportData = "",
                            carMake = "",
                            carNumber = "",
                            additionalCarNumber = "",
                            transportCompanyCode = ""
                    )
            )
        }
    }

    private fun clickNextByTaskShipmentRCByStatusRecounted() {
        if (taskManager.getReceivingTask()?.taskDescription?.submergedGE?.isNotEmpty() == true) {
            screenNavigator.openShipmentAdjustmentConfirmationDialog(
                    submergedGE = taskManager.getReceivingTask()?.taskDescription?.submergedGE.orEmpty(),
                    nextCallbackFunc = {
                        screenNavigator.openShipmentPostingLoadingScreen()
                    }
            )
        } else {
            screenNavigator.openShipmentPostingLoadingScreen()
        }
    }

    private fun clickNextByTaskShipmentPP() {
        //ТП для ОПП. 5.5.3	Разработка карточки задания на отгрузку в МП GRZ
        when (currentStatus.value) {
            TaskStatus.ReadyToShipment -> screenNavigator.openStartReviseLoadingScreen()
            TaskStatus.Checked -> screenNavigator.openRecountStartLoadingScreen()
            TaskStatus.Recounted -> {
                if (isBksDiff) {
                    screenNavigator.openShipmentConfirmDiscrepanciesDialog(screenNavigator::openTransmittedLoadingScreen)
                } else {
                    screenNavigator.openTransmittedLoadingScreen()
                }
            }
            else -> Unit
        }
    }

    private fun clickNextByCurrentStatus() {
        when (taskManager.getReceivingTask()?.taskDescription?.currentStatus) {
            TaskStatus.Ordered, TaskStatus.Traveling, TaskStatus.TemporaryRejected -> clickNextByStatusOrderedTravelingTemporaryRejected()
            TaskStatus.Arrived -> clickNextByStatusArrived()
            TaskStatus.Checked -> screenNavigator.openStartConditionsReviseLoadingScreen()
            TaskStatus.Unloaded -> clickNextByStatusUnloaded()
            TaskStatus.Recounted -> screenNavigator.openTransmittedLoadingScreen()
            TaskStatus.Departure -> screenNavigator.openStartReviseLoadingScreen()
            TaskStatus.Booked -> screenNavigator.openTransferGoodsSectionScreen()
            TaskStatus.Completed -> screenNavigator.openFormedDocsScreen()
            else -> Unit
        }
    }

    private fun clickNextByStatusOrderedTravelingTemporaryRejected() {
        val currentStatus = taskManager.getReceivingTask()?.taskDescription?.currentStatus
                ?: TaskStatus.Other
        if (isEdoForTaskType(currentStatus)) { //trello https://trello.com/c/XKpOCvZo
            onOpenEdoDialog(currentStatus)
        } else {
            screenNavigator.openRegisterArrivalLoadingScreen()
        }
    }

    private fun onOpenEdoDialog(currentStatus: TaskStatus) {
        screenNavigator.openEdoDialog(
                missing = {
                    screenNavigator.openRegisterArrivalLoadingScreen(
                            isInStockPaperTTN = false,
                            isEdo = true,
                            status = currentStatus
                    )
                },
                inStock = {
                    screenNavigator.openRegisterArrivalLoadingScreen(
                            isInStockPaperTTN = true,
                            isEdo = true,
                            status = currentStatus
                    )
                }
        )
    }

    private fun isEdoForTaskType(currentStatus: TaskStatus): Boolean {
        val taskType = taskManager.getTaskType()
        return isEdo
                && taskType == TaskType.DirectSupplier
                && (currentStatus == TaskStatus.Ordered || currentStatus == TaskStatus.TemporaryRejected)
    }

    private fun clickNextByStatusArrived() {
        when {
            isEdoValidForInboundDelivery() -> {
                screenNavigator.openCreateInboundDeliveryDialog { screenNavigator.openStartReviseLoadingScreen() }
            }
            isTaskTypeValidForStartRDSLoading() -> {
                screenNavigator.openUnloadingStartRDSLoadingScreen()
            }
            else -> {
                screenNavigator.openStartReviseLoadingScreen()
            }
        }
    }

    private fun isEdoValidForInboundDelivery(): Boolean {
        return isEdo
                && taskType == TaskType.DirectSupplier
                && incomingDelivery.isEmpty()
    }

    private fun isTaskTypeValidForStartRDSLoading(): Boolean {
        return taskType == TaskType.ReceptionDistributionCenter
                || taskType == TaskType.OwnProduction
                || taskType == TaskType.ShoppingMall
    }

    private fun clickNextByStatusUnloaded() {
        when (taskManager.getTaskType()) {
            TaskType.ReceptionDistributionCenter, TaskType.ShoppingMall -> {
                screenNavigator.openNoTransportDefectDeclaredDialog { nextCallbackFuncOpenNoTransportDefectDeclaredDialog() }
            }
            TaskType.OwnProduction -> fixationDeparture()
            else -> screenNavigator.openRecountStartLoadingScreen()
        }
    }

    private fun nextCallbackFuncOpenNoTransportDefectDeclaredDialog() {
        if (taskManager.getReceivingTask()?.taskDescription?.quantityOutgoingFillings == 0) {
            fixationDeparture()
        } else {
            screenNavigator.openInputOutgoingFillingsScreen()
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }

    private fun shipmentStartRecount() {
        launchUITryCatch {
            screenNavigator.showProgressLoadingData(::handleFailure)
            val params = ZmpUtzGrz39V001Params(
                    taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber.orEmpty(),
                    deviceIP = context.getDeviceIp(),
                    personalNumber = sessionInfo.personnelNumber.orEmpty(),
                    recountStartDate = taskManager.getReceivingTask()?.taskDescription?.nextStatusDate.orEmpty(),
                    recountStartTime = taskManager.getReceivingTask()?.taskDescription?.nextStatusTime.orEmpty()
            )
            zmpUtzGrz39V001NetRequest(params).either(::handleFailure, ::handleSuccessShipmentStartRecount)
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccessShipmentStartRecount(result: ZmpUtzGrz39V001Result) {
        val cargoUnits = result.cargoUnits.map { TaskCargoUnitInfo.from(it) }
        taskManager.getReceivingTask()?.taskRepository?.getCargoUnits()?.updateCargoUnits(cargoUnits)

        screenNavigator.openControlDeliveryCargoUnitsScreen()
    }

    private fun fixationDeparture() {
        launchUITryCatch {
            screenNavigator.showProgressLoadingData(::handleFailure)
            val params = FixationDepartureReceptionDistrCenterParameters(
                    taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber.orEmpty(),
                    deviceIP = context.getDeviceIp(),
                    personalNumber = sessionInfo.personnelNumber.orEmpty(),
                    fillings = emptyList()
            )
            fixationDepartureReceptionDistrCenterNetRequest(params).either(::handleFailure, ::handleSuccessFixationDeparture)
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccessFixationDeparture(result: FixationDepartureReceptionDistrCenterResult) {
        val notifications = result.notifications.map { TaskNotification.from(it) }
        taskManager.getReceivingTask()?.taskRepository?.getNotifications()?.updateWithNotifications(general = notifications, document = null, product = null, condition = null)

        taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))

        screenNavigator.openTaskCardScreen(TaskCardMode.Full, taskManager.getTaskType())
    }

    private fun shipmentSkipRecount() {
        launchUITryCatch {
            screenNavigator.showProgress(context.getString(R.string.skipping_recount))
            val params = SkipRecountParameters(
                    taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber.orEmpty(),
                    deviceIP = context.getDeviceIp(),
                    personalNumber = sessionInfo.personnelNumber.orEmpty(),
                    comment = ""
            )
            skipRecountNetRequest(params).either(::handleFailure, ::handleSuccessSkipRecount)
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccessSkipRecount(result: SkipRecountResult) {
        launchUITryCatch {
            val notifications = result.notifications.map { TaskNotification.from(it) }
            val sectionInfo = result.sectionsInfo.map { TaskSectionInfo.from(it) }
            val sectionProducts = result.sectionProducts.map { TaskSectionProducts.from(hyperHive, it) }
            taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))
            taskManager.getReceivingTask()?.taskRepository?.getNotifications()?.updateWithNotifications(notifications, null, null, null)
            taskManager.getReceivingTask()?.taskRepository?.getSections()?.updateSections(sectionInfo, sectionProducts)
            screenNavigator.openTaskCardScreen(TaskCardMode.Full, taskManager.getTaskType())
        }
    }

    private fun shipmentAllowedByGis() {
        launchUITryCatch {
            screenNavigator.showProgressLoadingData(::handleFailure)
            val params = ZmpUtzGrz41V001Params(
                    deviceIP = context.getDeviceIp(),
                    taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber.orEmpty(),
                    personalNumber = sessionInfo.personnelNumber.orEmpty()
            )
            zmpUtzGrz41V001NetRequest(params).either(::handleFailure, ::handleSuccessShipmentAllowedByGis)
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccessShipmentAllowedByGis(result: ZmpUtzGrz41V001Result) {
        val notifications = result.notifications.map { TaskNotification.from(it) }
        taskManager.getReceivingTask()?.taskRepository?.getNotifications()?.updateWithNotifications(general = notifications, document = null, product = null, condition = null)

        taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))

        screenNavigator.openShipmentPostingSuccessfulDialog(
                nextCallbackFunc = {
                    screenNavigator.openTaskCardScreen(TaskCardMode.Full, taskManager.getTaskType())
                }
        )
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