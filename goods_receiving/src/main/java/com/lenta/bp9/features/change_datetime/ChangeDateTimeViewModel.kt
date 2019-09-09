package com.lenta.bp9.features.change_datetime

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskStatus
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.date_time.DateTimeUtil
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangeDateTimeViewModel : CoreViewModel() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var screenNavigator: IScreenNavigator

    var mode: ChangeDateTimeMode = ChangeDateTimeMode.None

    val taskCaption: String by lazy {
        taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    val screenDescription: String by lazy {
        val status = taskManager.getReceivingTask()?.taskDescription?.currentStatus
        if (status == TaskStatus.Checked)
            context.getString(R.string.unloading_start)
        else if (status == TaskStatus.Arrived && mode == ChangeDateTimeMode.NextStatus)
            context.getString(R.string.checking_start)
        else
            context.getString(R.string.register_arrival)
    }

    val dateCaption: String by lazy {
        val status = taskManager.getReceivingTask()?.taskDescription?.currentStatus
        if (status == TaskStatus.Checked)
            context.getString(R.string.unloading_date)
        else if (status == TaskStatus.Arrived && mode == ChangeDateTimeMode.NextStatus)
            context.getString(R.string.checking_date)
        else
            context.getString(R.string.arrival_date)
    }

    val timeCaption: String by lazy {
        val status = taskManager.getReceivingTask()?.taskDescription?.currentStatus
        if (status == TaskStatus.Checked)
            context.getString(R.string.unloading_time)
        else if (status == TaskStatus.Arrived && mode == ChangeDateTimeMode.NextStatus)
            context.getString(R.string.checking_time)
        else
            context.getString(R.string.arrival_time)
    }

    val days: MutableLiveData<String> = MutableLiveData("")
    val months: MutableLiveData<String> = MutableLiveData("")
    val years: MutableLiveData<String> = MutableLiveData("")
    val hours: MutableLiveData<String> = MutableLiveData("")
    val minutes: MutableLiveData<String> = MutableLiveData("")

    fun onResume() {
        viewModelScope.launch {
            val milliseconds = System.currentTimeMillis()
            days.value = DateTimeUtil.formatDate(milliseconds, Constants.DATE_FORMAT_dd)
            months.value = DateTimeUtil.formatDate(milliseconds, Constants.DATE_FORMAT_mm)
            years.value = DateTimeUtil.formatDate(milliseconds, Constants.DATE_FORMAT_yy)
            hours.value = DateTimeUtil.formatDate(milliseconds, Constants.TIME_FORMAT_HH)
            minutes.value = DateTimeUtil.formatDate(milliseconds, Constants.TIME_FORMAT_mm)
        }
    }

    fun onClickApply() {
        val dateString = days.value + "." + months.value + "." + years.value
        val timeString = hours.value + ":" + minutes.value
        if (mode == ChangeDateTimeMode.NextStatus) {
            taskManager.getReceivingTask()?.taskDescription?.nextStatusDate = dateString
            taskManager.getReceivingTask()?.taskDescription?.nextStatusTime = timeString
        } else {
            taskManager.getReceivingTask()?.taskDescription?.currentStatusDate = dateString
            taskManager.getReceivingTask()?.taskDescription?.currentStatusTime = timeString
        }
        screenNavigator.goBack()
    }
}

enum class ChangeDateTimeMode {
    None,
    CurrentStatus,
    NextStatus
}