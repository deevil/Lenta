package com.lenta.shared.scan

import android.app.Activity
import androidx.lifecycle.LiveData

interface IScanHelper {
    val scanResult: LiveData<String>
    fun startListen(activity: Activity)
    fun stopListen(activity: Activity)
}