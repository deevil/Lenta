package com.lenta.bp9.features.change_datetime

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskStatus
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.date_time.DateTimeUtil
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
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

    val mode: MutableLiveData<ChangeDateTimeMode> = MutableLiveData()

    val taskCaption: String by lazy {
        taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    val screenDescription: String by lazy {
        val status = taskManager.getReceivingTask()?.taskDescription?.currentStatus
        if (status == TaskStatus.Checked)
            context.getString(R.string.unloading_start)
        else if (status == TaskStatus.Arrived && mode.value == ChangeDateTimeMode.NextStatus)
            context.getString(R.string.checking_start)
        else
            context.getString(R.string.register_arrival)
    }

    val dateCaption: String by lazy {
        val status = taskManager.getReceivingTask()?.taskDescription?.currentStatus
        if (status == TaskStatus.Checked)
            context.getString(R.string.unloading_date)
        else if (status == TaskStatus.Arrived && mode.value == ChangeDateTimeMode.NextStatus)
            context.getString(R.string.checking_date)
        else
            context.getString(R.string.arrival_date)
    }

    val timeCaption: String by lazy {
        val status = taskManager.getReceivingTask()?.taskDescription?.currentStatus
        if (status == TaskStatus.Checked)
            context.getString(R.string.unloading_time)
        else if (status == TaskStatus.Arrived && mode.value == ChangeDateTimeMode.NextStatus)
            context.getString(R.string.checking_time)
        else
            context.getString(R.string.arrival_time)
    }

    val days: MutableLiveData<String> = MutableLiveData("")
    val months: MutableLiveData<String> = MutableLiveData("")
    val years: MutableLiveData<String> = MutableLiveData("")
    val hours: MutableLiveData<String> = MutableLiveData("")
    val minutes: MutableLiveData<String> = MutableLiveData("")
    val seconds: MutableLiveData<String> = MutableLiveData("")

    val enabledApplyButton: MutableLiveData<Boolean> = days.
            combineLatest(months).
            combineLatest(years).
            combineLatest(hours).
            combineLatest(minutes).map {
        val dateString = days.value + "." + months.value + "." + years.value
        val timeString = hours.value + ":" + minutes.value
        isCorrectDateTime("$dateString $timeString")
    }

    fun onResume() {
        viewModelScope.launch {
            val milliseconds = timeMonitor.getUnixTime()
            days.value = DateTimeUtil.formatDate(milliseconds, Constants.DATE_FORMAT_dd)
            months.value = DateTimeUtil.formatDate(milliseconds, Constants.DATE_FORMAT_mm)
            years.value = DateTimeUtil.formatDate(milliseconds, Constants.DATE_FORMAT_yy)
            hours.value = DateTimeUtil.formatDate(milliseconds, Constants.TIME_FORMAT_HH)
            minutes.value = DateTimeUtil.formatDate(milliseconds, Constants.TIME_FORMAT_mm)
            seconds.value = DateTimeUtil.formatDate(milliseconds, Constants.TIME_FORMAT_mmss).substring(3)
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun onClickApply() {
        var dateString = days.value + "." + months.value + "." + years.value
        var timeString = hours.value + ":" + minutes.value + ":" + seconds.value
        val formatter = SimpleDateFormat("dd.MM.yy HH:mm:ss")
        val date = formatter.parse("$dateString $timeString")
        dateString = DateTimeUtil.formatDate(date, Constants.DATE_FORMAT_yyyy_mm_dd)
        timeString = DateTimeUtil.formatDate(date, Constants.TIME_FORMAT_hhmmss)

        if (mode.value == ChangeDateTimeMode.NextStatus) {
            taskManager.getReceivingTask()?.taskDescription?.nextStatusDate = dateString
            taskManager.getReceivingTask()?.taskDescription?.nextStatusTime = timeString!!
        } else {
            taskManager.getReceivingTask()?.taskDescription?.currentStatusDate = dateString
            taskManager.getReceivingTask()?.taskDescription?.currentStatusTime = timeString!!
        }
        screenNavigator.goBack()
    }

    @SuppressLint("SimpleDateFormat")
    private fun isCorrectDateTime(checkDateTime: String?): Boolean {
        return try {
            val formatter = SimpleDateFormat("dd.MM.yy HH:mm")
            val date = formatter.parse(checkDateTime)
            !(checkDateTime != formatter.format(date) || date!! > timeMonitor.getServerDate())
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