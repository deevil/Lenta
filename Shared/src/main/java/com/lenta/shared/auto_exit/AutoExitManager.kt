package com.lenta.shared.auto_exit

import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.coroutine.timer
import com.lenta.shared.utilities.runIfRelease
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class AutoExitManager(private var onNeedExitCallback: () -> Unit) {

    var autoExitTimeInMinutes: Long? = null

    private var lastTimeInMillis = Long.MIN_VALUE

    init {
        runIfRelease {
            GlobalScope.launch {
                timer(1000) {
                    checkLastTime()
                }

            }
        }
    }


    fun checkLastTime() {
        Logg.d { "checkLastTime" }
        autoExitTimeInMinutes?.let {
            if ((System.currentTimeMillis() - lastTimeInMillis) > (it * 1000 * 60)) {
                onNeedExitCallback()
            }
        }

    }

    fun setLastActiveTime() {
        lastTimeInMillis = System.currentTimeMillis()
    }

}