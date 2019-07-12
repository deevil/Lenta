package com.lenta.shared.analytics

interface IAnalytics {
    fun init()
    fun logWarning(tag: String? = null, message: String)
    fun logFatal(tag: String? = null, message: String)
    fun sendLogs()
    fun cleanLogs()

}