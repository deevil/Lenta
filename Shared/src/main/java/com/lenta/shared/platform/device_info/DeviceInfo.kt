package com.lenta.shared.platform.device_info

import android.content.Context
import com.lenta.shared.utilities.extentions.getDeviceId
import com.lenta.shared.utilities.extentions.getDeviceIp

class AndroidDeviceInfo(private val context: Context) : DeviceInfo {
    override fun getDeviceIp(): String {
        return context.getDeviceIp()
    }

    override fun getDeviceId(): String {
        return context.getDeviceId()
    }
}

interface DeviceInfo {
    fun getDeviceIp(): String
    fun getDeviceId(): String
}