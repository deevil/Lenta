package com.lenta.shared.platform.time

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.constants.Constants.DATE_FORMAT_yyyy_mm_dd
import com.lenta.shared.platform.constants.Constants.TIME_FORMAT_hhmmss
import com.lenta.shared.utilities.coroutine.timer
import com.lenta.shared.utilities.date_time.DateTimeUtil.getDateFromString
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TimeMonitor(private val intervalInMsec: Long) : ITimeMonitor {

    private var deltaTime: Long = 0

    private val unixTime: MutableLiveData<Long> = MutableLiveData(0)

    init {
        GlobalScope.launch {
            timer(intervalInMsec) {
                unixTime.postValue(System.currentTimeMillis() + deltaTime)
            }
        }
    }

    override fun observeUnixTime(): LiveData<Long> {
        return unixTime
    }

    override fun setServerTime(time: String, date: String) {
        val dateTime = date + time
        val serverDate = getDateFromString(dateTime, DATE_FORMAT_yyyy_mm_dd + TIME_FORMAT_hhmmss)
        deltaTime = serverDate.time - System.currentTimeMillis()
    }
}

interface ITimeMonitor {
    fun observeUnixTime(): LiveData<Long>
    fun setServerTime(time: String, date: String)
}