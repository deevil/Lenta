package com.lenta.shared.utilities.extentions

import androidx.lifecycle.viewModelScope
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.*

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

fun CoreViewModel.launchAsync(
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
) {
    viewModelScope.launch(viewModelScope.coroutineContext + Dispatchers.IO, start, block)
}

fun CoreViewModel.launchAsyncTryCatch(catchBlock: ((Throwable) -> Unit)? = null, tryBlock: suspend CoroutineScope.() -> Unit) {
    try {
        launchAsync(CoroutineStart.DEFAULT, tryBlock)
    } catch (e: Throwable) {
        catchBlock?.invoke(e) ?: handleFailure(failure = Failure.ThrowableFailure(e))
    }
}

fun CoreViewModel.handleLoadingTimeOut(handleFailure: () -> Unit) {
    viewModelScope.coroutineContext.cancel()
    handleFailure.invoke()
}
