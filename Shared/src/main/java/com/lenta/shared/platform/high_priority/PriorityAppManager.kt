package com.lenta.shared.platform.high_priority


import android.content.Context
import android.content.Intent

import javax.inject.Inject

class PriorityAppManager @Inject constructor(private val context: Context) {

    fun setHighPriority() {
        sendAction(MainService.ACTION_HIGH_PRIORITY)
    }

    fun setLowPriority() {
        sendAction(MainService.ACTION_LOW_PRIORITY)
    }

    private fun sendAction(action: String) {
        val intent = Intent(context, MainService::class.java)
        intent.action = action
        context.startService(intent)
    }

}
