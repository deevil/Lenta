package com.lenta.bp9.features.change_datetime

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.R
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskStatus
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.date_time.DateTimeUtil
import com.lenta.shared.utilities.extentions.launchUITryCatch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ChangeDateTimeViewModel : CoreViewModel() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var timeMonitor: ITimeMonitor
    @Inject
    lateinit var dataBase: IDataBaseRepo

    val mode: MutableLiveData<ChangeDateTimeMode> = MutableLiveData()
    private val permittedNumberDays: MutableLiveData<Int> = MutableLiveData()

    val taskCaption: String by lazy {
        taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    val screenDescription: String by lazy {
        val currentStatus = taskManager.getReceivingTask()?.taskDescription?.currentStatus
        val taskType = taskManager.getReceivingTask()?.taskHeader?.taskType
        if (currentStatus == TaskStatus.Checked)
            context.getString(R.string.unloading_start)
        else if (currentStatus == TaskStatus.Arrived && mode.value == ChangeDateTimeMode.NextStatus && taskType != TaskType.ShipmentRC)
            context.getString(R.string.checking_start)
        else if (currentStatus == TaskStatus.Arrived && mode.value == ChangeDateTimeMode.NextStatus && taskType == TaskType.ShipmentRC)
            context.getString(R.string.loading_start)
        else
            context.getString(R.string.register_arrival)
    }

    val dateCaption: String by lazy {
        val currentStatus = taskManager.getReceivingTask()?.taskDescription?.currentStatus
        val taskType = taskManager.getReceivingTask()?.taskHeader?.taskType
        if (currentStatus == TaskStatus.Checked)
            context.getString(R.string.unloading_date)
        else if (currentStatus == TaskStatus.Arrived && mode.value == ChangeDateTimeMode.NextStatus && taskType != TaskType.ShipmentRC)
            context.getString(R.string.checking_date)
        else if (currentStatus == TaskStatus.Arrived && mode.value == ChangeDateTimeMode.NextStatus && taskType == TaskType.ShipmentRC)
            context.getString(R.string.loading_date)
        else
            context.getString(R.string.arrival_date)
    }

    val timeCaption: String by lazy {
        val currentStatus = taskManager.getReceivingTask()?.taskDescription?.currentStatus
        val taskType = taskManager.getReceivingTask()?.taskHeader?.taskType
        if (currentStatus == TaskStatus.Checked)
            context.getString(R.string.unloading_time)
        else if (currentStatus == TaskStatus.Arrived && mode.value == ChangeDateTimeMode.NextStatus && taskType != TaskType.ShipmentRC)
            context.getString(R.string.checking_time)
        else if (currentStatus == TaskStatus.Arrived && mode.value == ChangeDateTimeMode.NextStatus && taskType == TaskType.ShipmentRC)
            context.getString(R.string.loading_time)
        else
            context.getString(R.string.arrival_time)
    }

    val days: MutableLiveData<String> = MutableLiveData("")
    val months: MutableLiveData<String> = MutableLiveData("")
    val years: MutableLiveData<String> = MutableLiveData("")
    val hours: MutableLiveData<String> = MutableLiveData("")
    val minutes: MutableLiveData<String> = MutableLiveData("")
    private val seconds: MutableLiveData<String> = MutableLiveData("")

    fun onResume() {
        launchUITryCatch {
            val milliseconds = timeMonitor.getUnixTime()
            days.value = DateTimeUtil.formatDate(milliseconds, Constants.DATE_FORMAT_dd)
            months.value = DateTimeUtil.formatDate(milliseconds, Constants.DATE_FORMAT_mm)
            years.value = DateTimeUtil.formatDate(milliseconds, Constants.DATE_FORMAT_yy)
            hours.value = DateTimeUtil.formatDate(milliseconds, Constants.TIME_FORMAT_HH)
            minutes.value = DateTimeUtil.formatDate(milliseconds, Constants.TIME_FORMAT_mm)
            seconds.value = DateTimeUtil.formatDate(milliseconds, Constants.TIME_FORMAT_mmss).substring(3)
            permittedNumberDays.value = dataBase.getParamPermittedNumberDays()?.toInt()
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun onClickApply() {
        var dateString = days.value + "." + months.value + "." + years.value
        var timeString = hours.value + ":" + minutes.value + ":" + seconds.value

        if (!isCorrectDateTime("$dateString $timeString")) {
            screenNavigator.openDateNotCorrectlyScreen()
            return
        }

        val formatter = SimpleDateFormat("dd.MM.yy HH:mm:ss")
        val date = formatter.parse("$dateString $timeString")
        dateString = DateTimeUtil.formatDate(date, Constants.DATE_FORMAT_yyyy_mm_dd)
        timeString = DateTimeUtil.formatDate(date, Constants.TIME_FORMAT_hhmmss)

        if (mode.value == ChangeDateTimeMode.NextStatus) {
            taskManager.getReceivingTask()?.taskDescription?.nextStatusDate = dateString
            taskManager.getReceivingTask()?.taskDescription?.nextStatusTime = timeString
        } else {
            taskManager.getReceivingTask()?.taskDescription?.currentStatusDate = dateString
            taskManager.getReceivingTask()?.taskDescription?.currentStatusTime = timeString
        }
        screenNavigator.goBack()
    }

    @SuppressLint("SimpleDateFormat")
    private fun isCorrectDateTime(checkDateTime: String?): Boolean {
        return try {
            val taskType = taskManager.getReceivingTask()?.taskHeader?.taskType ?: TaskType.None
            val selectedDateTime = SimpleDateFormat("dd.MM.yy HH:mm:ss").parse(checkDateTime)
            if (mode.value == ChangeDateTimeMode.NextStatus) {
                val minNextStatusDate = Calendar.getInstance()
                minNextStatusDate.time = SimpleDateFormat("yyyy-MM-dd").parse(taskManager.getReceivingTask()?.taskDescription?.nextStatusDate)
                minNextStatusDate.add(Calendar.DATE, (permittedNumberDays.value ?: 0) * -1)
                if (taskManager.getReceivingTask()?.taskDescription?.currentStatus == TaskStatus.Traveling
                        && (taskType == TaskType.DirectSupplier
                                || taskType == TaskType.ReceptionDistributionCenter
                                || taskType == TaskType.OwnProduction
                                || taskType == TaskType.ShoppingMall)) {
                    selectedDateTime <= timeMonitor.getServerDate() && selectedDateTime >= minNextStatusDate.time
                } else {
                    val currentStatusDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("${taskManager.getReceivingTask()?.taskDescription?.currentStatusDate} ${taskManager.getReceivingTask()?.taskDescription?.currentStatusTime}")
                    selectedDateTime <= timeMonitor.getServerDate() && selectedDateTime >= currentStatusDateTime
                }
            } else {
                selectedDateTime <= timeMonitor.getServerDate()
            }
        } catch (e: Exception) {
            false
        }
    }
}

enum class ChangeDateTimeMode {
    None,
    CurrentStatus,
    NextStatus
}