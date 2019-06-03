package com.lenta.shared.analytics

import androidx.annotation.WorkerThread
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject
import kotlin.reflect.KFunction1

class FmpAnalytics @Inject constructor(val hyperHive: HyperHive) : IAnalytics {

    override fun init() {
        hyperHive.loggingAPI.initAutoSendingLogs()
    }


    override fun logWarning(tag: String?, message: String) {
        enableLogsFuncDisableLogs(tag, message, hyperHive.loggingAPI::logWarning)
    }

    override fun logFatal(tag: String?, message: String) {
        enableLogsFuncDisableLogs(tag, message, hyperHive.loggingAPI::logFatal)
    }


    override fun cleanLogs() {
        hyperHive.databaseAPI.query(hyperHive.loggingAPI.loggingBasePath, "DELETE FROM hyperhive_journal").execute()
    }

    @WorkerThread
    override fun sendLogs() {

        hyperHive.loggingAPI.sendLogs().execute()
    }

    private fun enableLogsFuncDisableLogs(tag: String?, message: String, funcSendLogs: KFunction1<@ParameterName(name = "message") String, Unit>) {
        enableLogs()
        funcSendLogs("${tag ?: ""}\n$message")
        disableLogs()
    }

    private fun enableLogs() {
        hyperHive.loggingAPI.setLogLevel(4)
    }

    private fun disableLogs() {
        hyperHive.loggingAPI.setLogLevel(10)
    }


}

interface IAnalytics {
    fun init()
    fun logWarning(tag: String? = null, message: String)
    fun logFatal(tag: String? = null, message: String)
    fun sendLogs()
    fun cleanLogs()

}