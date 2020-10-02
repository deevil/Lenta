package com.lenta.shared.analytics


interface IAnalytics {
    fun enableLogs(enable: Boolean)
    fun init()
    fun logTrace(tag: String? = null, message: String?)
    fun logError(tag: String? = null, message: String?)
    fun logFatal(tag: String? = null, message: String?)
    fun sendLogs()
    fun cleanLogs()

}