package com.lenta.movement.features.task

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.movement.models.*
import com.lenta.movement.platform.IFormatter
import com.lenta.movement.platform.extensions.unsafeLazy
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.movement.requests.network.ApprovalAndTransferToTasksCargoUnit
import com.lenta.movement.requests.network.ApprovalAndTransferToTasksCargoUnitParams
import com.lenta.movement.requests.network.StartConsolidation
import com.lenta.movement.requests.network.StartConsolidationParams
import com.lenta.movement.requests.network.models.toModelList
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.GisControl
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.date_time.DateTimeUtil
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toSapBooleanString
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.lang.Exception
import java.util.*
import javax.inject.Inject

class TaskViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var taskManager: ITaskManager

    @Inject
    lateinit var formatter: IFormatter

    @Inject
    lateinit var startConsolidation: StartConsolidation

    @Inject
    lateinit var approvalAndTransferToTasksCargoUnit: ApprovalAndTransferToTasksCargoUnit

    val task by unsafeLazy { MutableLiveData(taskManager.getTaskOrNull()) }
    private val currentStatus: Task.Status
        get() = task.value?.currentStatus ?: Task.Status.Created()
    private val nextStatus: Task.Status
        get() = task.value?.nextStatus ?: Task.Status.Counted()
    private val taskType: TaskType
        get() = task.value?.taskType ?: TaskType.TransferWithoutOrder
    private val movementType: MovementType
        get() = task.value?.movementType ?: MovementType.SS
    private val setting: TaskSettings
        get() = taskManager.getTaskSettings(taskType, movementType)

    val selectedPagePosition = MutableLiveData(0)

    val currentStatusText by lazy { formatter.getTaskStatusName(currentStatus) }
    val nextStatusText by lazy { formatter.getTaskStatusName(nextStatus) }

    val taskTypeEnabled = MutableLiveData(false)
    val taskTypesFormatted by lazy {
        MutableLiveData(TaskType.values().map { formatter.getTaskTypeNameDescription(it) })
    }
    val taskTypeSelectedPosition by lazy { MutableLiveData(taskType.ordinal) }

    val movementTypeEnabled = MutableLiveData(false)
    val movementTypesFormatted by lazy {
        MutableLiveData(MovementType.values().map { formatter.getMovementTypeNameDescription(it) })
    }
    val movementSelectedPosition by lazy { MutableLiveData(movementType.ordinal) }

    val taskNameEnabled by lazy { task.map { it == null } }
    val taskName by lazy {
        val defaultName = "Перемещение от ${DateTimeUtil.formatDate(Date(), Constants.DATE_FORMAT_dd_mm_yyyy_hh_mm)}"
        MutableLiveData(task.value?.name ?: defaultName)
    }

    val receiversEnabled by lazy {
        MediatorLiveData<Boolean>().apply {
            addSource(receivers) { value = task.value == null && it.size != 1 }
            addSource(task) { value = it == null && receivers.value?.size != 1 }
        }
    }
    val receivers by lazy {
        task.map { taskOrNull ->
            taskOrNull?.receiver?.let { listOf(it) } ?: taskManager.getAvailableReceivers()
        }
    }
    val receiverSelectedPosition = MutableLiveData(0)
    val receiverSelectedPositionListener = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            receiverSelectedPosition.value = position
        }
    }

    val pikingStorageListEnabled by lazy {
        MediatorLiveData<Boolean>().apply {
            addSource(pikingStorageList) { value = task.value == null && it.size != 1 }
            addSource(task) { value = it == null && pikingStorageList.value?.size != 1 }
        }
    }
    val pikingStorageList by lazy {
        task.map { taskOrNull ->
            taskOrNull?.pikingStorage?.let { listOf(it) }
                    ?: taskManager.getAvailablePikingStorageList(taskType, movementType).addFirstEmptyIfNeeded()
        }
    }
    val pikingStorageSelectedPosition = MutableLiveData(0)
    val pikingStorageSelectedPositionListener = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            pikingStorageSelectedPosition.value = position
        }
    }

    val shipmentStorageListEnabled by lazy {
        MediatorLiveData<Boolean>().apply {
            addSource(shipmentStorageList) { value = task.value == null && it.size != 1 }
            addSource(task) { value = it == null && shipmentStorageList.value?.size != 1 }
        }
    }
    val shipmentStorageList by lazy {
        task.map { taskOrNull ->
            taskOrNull?.shipmentStorage
                    ?.let { listOf(it) }
                    ?: setting.shipmentStorageList
                            .addFirstEmptyIfNeeded()
        }
    }

    val shipmentStorageSelectedPosition = MutableLiveData(0)
    val shipmentStorageSelectedPositionListener = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            shipmentStorageSelectedPosition.postValue(position)
        }
    }

    val shipmentDateEnabled by lazy { task.map { it == null } }
    val shipmentDate by lazy {
        val date = task.value?.shipmentDate?.let {
            DateTimeUtil.formatDate(it, Constants.DATE_FORMAT_ddmmyy)
        }
        val defaultDate = DateTimeUtil.formatDate(Date(), Constants.DATE_FORMAT_ddmmyy)
        MutableLiveData(date ?: defaultDate)
    }

    val description by lazy { MutableLiveData(setting.description) }
    val comments by lazy { MutableLiveData(task.value?.comment.orEmpty()) }

    val alcoVisible by lazy {
        MutableLiveData(setting.gisControls.contains(GisControl.Alcohol))
    }
    val generalVisible by lazy {
        MutableLiveData(setting.gisControls.contains(GisControl.GeneralProduct))
    }

    val nextEnabled by lazy {
        MediatorLiveData<Boolean>().apply {
            addSource(taskName) { value = validate() }
            addSource(receiverSelectedPosition) { value = validate() }
            addSource(pikingStorageSelectedPosition) { value = validate() }
            addSource(shipmentStorageSelectedPosition) { value = validate() }
            addSource(shipmentDate) { value = validate() }
        }
    }

    fun getTitle(): String {
        return formatter.formatMarketName(sessionInfo.market.orEmpty())
    }

    override fun onPageSelected(position: Int) {
        selectedPagePosition.value = position
    }

    fun onNextClick() {
        if (task.value == null) {
            taskManager.setTask(buildTask())
            screenNavigator.openTaskCompositionScreen()
        } else {
            Logg.d {
                currentStatus.toString()
            }
            when (currentStatus) {
                Task.Status.ToConsolidation(Task.Status.TO_CONSOLIDATION) -> {
                    viewModelScope.launch {
                        startConsolidation(
                                StartConsolidationParams(
                                        deviceIp = context.getDeviceIp(),
                                        taskNumber = task.value!!.number,
                                        mode = GET_TASK_COMP_CODE,
                                        personnelNumber = sessionInfo.personnelNumber!!,
                                        withProductInfo = true.toSapBooleanString()
                                )
                        ).either({
                            screenNavigator.openAlertScreen(it)
                        }, {
                            screenNavigator.openTaskEoMergeScreen(it.eoList.toModelList(), it.geList)
                        }
                        )
                    }
                }

                Task.Status.Consolidated(Task.Status.CONSOLIDATED) -> {
                    viewModelScope.launch {
                        approvalAndTransferToTasksCargoUnit(
                                ApprovalAndTransferToTasksCargoUnitParams(
                                        deviceIp = context.getDeviceIp(),
                                        taskNumber = task.value!!.number,
                                        personnelNumber = sessionInfo.personnelNumber!!
                                )
                        ).either({
                            screenNavigator.openAlertScreen(it)
                        }, {
                            Logg.d{"Approval and transfer to task cargo unit: $it"}
                        })
                    }
                }
            }

            screenNavigator.openNotImplementedScreenAlert("Состав сохраненного задания")
            return
        }

    }

    fun onBackPressed() {
        taskManager.clear()
        screenNavigator.goBack()
    }

    private fun validate(): Boolean {
        if (task.value != null) {
            return true
        }

        return buildTask().let { task ->
            task.name.isNotEmpty() &&
                    task.receiver.isNotEmpty() &&
                    task.pikingStorage.isNotEmpty() &&
                    task.shipmentStorage.isNotEmpty() &&
                    task.shipmentDate.after(DateTime.now().minusDays(1).toDate())
        }
    }

    private fun buildTask(): Task {
        return Task(
                isCreated = false,
                number = "",
                currentStatus = currentStatus,
                nextStatus = nextStatus,
                name = taskName.value.orEmpty(),
                comment = comments.value.orEmpty(),
                taskType = taskType,
                movementType = movementType,
                receiver = receivers.getSelectedValue(receiverSelectedPosition).orEmpty(),
                pikingStorage = pikingStorageList.getSelectedValue(pikingStorageSelectedPosition).orEmpty(),
                shipmentStorage = shipmentStorageList.getSelectedValue(shipmentStorageSelectedPosition).orEmpty(),
                shipmentDate = shipmentDate.value?.toDate() ?: Date(0)
        )
    }

    private fun String.toDate(): Date? {
        return try {
            DateTimeUtil.getDateFromString(this, Constants.DATE_FORMAT_ddmmyy)
        } catch (_: Exception) {
            null
        }
    }

    private fun List<String>.addFirstEmptyIfNeeded(): List<String> {
        return if (size > 1) {
            listOf("") + this
        } else {
            this
        }
    }

    private fun <T> LiveData<List<T>>.getSelectedValue(position: LiveData<Int>): T? {
        return value?.getOrNull(position.value ?: -1)
    }

    companion object {
        private const val GET_TASK_COMP_CODE = 1
        private const val GET_TASK_COMP_WITH_BLOCK = 2
    }

}