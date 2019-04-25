package com.lenta.shared.features.loading

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.utilities.coroutine.timer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


fun startProgressTimer(
        coroutineScope: CoroutineScope,
        elapsedTime: MutableLiveData<Long>? = null,
        remainingTime: MutableLiveData<Long>? = null,
        timeoutInSec: Int? = null) : Job {

    val startTime = System.currentTimeMillis()

    return coroutineScope.launch {
        elapsedTime?.postValue(0)
        timeoutInSec?.let {
            remainingTime?.postValue((timeoutInSec * 1000).toLong())
        }

        timer(1000) {
            val elapsTime = System.currentTimeMillis() - startTime
            elapsedTime?.postValue(elapsTime)
            timeoutInSec?.let {
                remainingTime?.postValue((timeoutInSec * 1000) - elapsTime)
            }
        }
    }

}
