package com.lenta.shared.auto_exit


class AutoExitManager(private var onNeedExitCallback: () -> Unit) {

    var autoExitTimeInMinutes: Long? = null

    private var lastTimeInMillis = Long.MIN_VALUE


    fun checkLastTime() {
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