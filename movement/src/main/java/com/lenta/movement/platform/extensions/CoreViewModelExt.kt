package com.lenta.movement.platform.extensions

import androidx.lifecycle.viewModelScope
import com.lenta.movement.exception.ThrowableFailure
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
        catchBlock?.invoke(e) ?: handleFailure(failure = ThrowableFailure(e))
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
        catchBlock?.invoke(e) ?: handleFailure(failure = ThrowableFailure(e))
    }
}