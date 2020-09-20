package com.lenta.shared.utilities.extentions

import androidx.lifecycle.LiveDataScope
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun CoreViewModel.launchUITryCatch(
        start: CoroutineStart = CoroutineStart.DEFAULT,
        catchBlock: ((Throwable) -> Unit)? = null, tryBlock: suspend CoroutineScope.() -> Unit
) {
    try {
        viewModelScope.launch(viewModelScope.coroutineContext, start, tryBlock)
    } catch (e: Throwable) {
        catchBlock?.invoke(e) ?: handleFailure(failure = Failure.ThrowableFailure(e))
    }
}

fun CoreViewModel.launchAsyncTryCatch(catchBlock: ((Throwable) -> Unit)? = null, tryBlock: suspend CoroutineScope.() -> Unit) {
    try {
        launchAsync(CoroutineStart.DEFAULT, tryBlock)
    } catch (e: Throwable) {
        catchBlock?.invoke(e) ?: handleFailure(failure = Failure.ThrowableFailure(e))
    }
}

fun CoreViewModel.launchAsync(
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
) {
    viewModelScope.launch(viewModelScope.coroutineContext + Dispatchers.IO, start, block)
}

inline fun <reified T> CoreViewModel.asyncLiveData(
        noinline block: suspend LiveDataScope<T>.() -> Unit
) = liveData(context = viewModelScope.coroutineContext + Dispatchers.IO, block = block)

fun <T> CoreViewModel.asyncTryCatchLiveData(
        catchBlock: ((Throwable) -> Unit)? = null,
        tryBlock: suspend () -> T
) = liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
    try {
        emit(tryBlock())
    } catch (e: Throwable) {
        catchBlock?.invoke(e) ?: handleFailure(failure = Failure.ThrowableFailure(e))
    }
}