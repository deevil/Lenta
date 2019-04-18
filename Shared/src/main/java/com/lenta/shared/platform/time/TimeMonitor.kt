package com.lenta.shared.platform.time

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.utilities.coroutine.timer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TimeMonitor(private val intervalInMsec: Long) : ITimeMonitor {

    private val unixTime: MutableLiveData<Long> = MutableLiveData(0)

    init {
        GlobalScope.launch {
            timer(intervalInMsec) {
                unixTime.postValue(System.currentTimeMillis())
            }
        }
    }

    override fun observeUnixTime(): LiveData<Long> {
        return unixTime
    }


}

interface ITimeMonitor {
    fun observeUnixTime(): LiveData<Long>
}