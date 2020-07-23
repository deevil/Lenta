package com.lenta.shared.features.loading

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.exception.Failure
import com.lenta.shared.utilities.coroutine.timer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun startProgressTimer(
        coroutineScope: CoroutineScope,
        elapsedTime: MutableLiveData<Long>? = null,
        remainingTime: MutableLiveData<Long>? = null,
        timeoutInSec: Int? = null,
        handleFailure: ((Failure) -> Unit)? = null
): Job {

    val startTime = System.currentTimeMillis()
    val timeOutInMills = timeoutInSec?.times(1000) ?: 0

    return coroutineScope.launch {
        elapsedTime?.postValue(0)

        timeoutInSec?.let {
            remainingTime?.postValue(timeOutInMills.toLong())
        }

        timer(1000) {
            val elapsed = System.currentTimeMillis() - startTime
            elapsedTime?.postValue(elapsed)

            timeoutInSec?.let {
                if (elapsed > timeOutInMills) {
                    handleFailure?.invoke(Failure.TimeOutError)
                }

                remainingTime?.postValue(timeOutInMills - elapsed)
            }
        }
    }

}
