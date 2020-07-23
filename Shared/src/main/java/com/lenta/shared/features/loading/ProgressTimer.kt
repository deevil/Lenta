package com.lenta.shared.features.loading

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.exception.Failure
import com.lenta.shared.utilities.coroutine.timer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

fun startProgressTimer(
        coroutineScope: CoroutineScope,
        elapsedTime: MutableLiveData<Long>? = null,
        remainingTime: MutableLiveData<Long>? = null,
        timeoutInSec: Int? = null,
        hideProgress: (() -> Unit)? = null,
        handleFailure: ((Failure) -> Unit)? = null
) {

    val startTime = System.currentTimeMillis()
    val timeOutInMills = timeoutInSec?.times(1000L) ?: 0L

    elapsedTime?.postValue(0)

    timeoutInSec?.let {
        remainingTime?.postValue(timeOutInMills)
    }

    coroutineScope.timer(1000) {
        val elapsed = System.currentTimeMillis() - startTime
        elapsedTime?.postValue(elapsed)

        timeoutInSec?.let {
            if (elapsed > timeOutInMills) {
                coroutineScope.cancel()
                hideProgress?.invoke()
                handleFailure?.invoke(Failure.TimeOutError)
            }

            remainingTime?.postValue(timeOutInMills - elapsed)
        }
    }

}