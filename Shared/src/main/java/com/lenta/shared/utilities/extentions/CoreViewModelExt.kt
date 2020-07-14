package com.lenta.shared.utilities.extentions

import androidx.lifecycle.viewModelScope
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun CoreViewModel.launchAsync(
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
) {
    viewModelScope.launch(viewModelScope.coroutineContext + Dispatchers.IO, start, block)
}

fun CoreViewModel.launchAsyncTryCatch(catchBlock: ((Throwable) -> Unit)? = null, tryBlock: suspend CoroutineScope.()->Unit) {
    try {
        launchAsync(CoroutineStart.DEFAULT, tryBlock)
    } catch (e: Throwable) {
        catchBlock?.invoke(e) ?: handleFailure(failure = Failure.ThrowableFailure(e))
    }
}