package com.lenta.shared.utilities.coroutine

import com.lenta.shared.utilities.Logg
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

fun CoroutineScope.timer(interval: Long, fixedRate: Boolean = true, action: suspend TimerScope.() -> Unit): Job {
    return launch {
        val scope = TimerScope()

        while (true) {
            val time = measureTimeMillis {
                try {
                    action(scope)
                } catch (ex: Exception) {
                    Logg.e { "Timer task exception: $ex" }
                }
            }

            if (scope.isCanceled) {
                break
            }

            if (fixedRate) {
                delay(Math.max(0, interval - time))
            } else {
                delay(interval)
            }

            yield()
        }
    }
}

class TimerScope {
    var isCanceled: Boolean = false
        private set

    fun cancel() {
        isCanceled = true
    }
}

fun CoroutineScope.start(block: suspend CoroutineScope.() -> Unit) {
    launch(block = block)
}