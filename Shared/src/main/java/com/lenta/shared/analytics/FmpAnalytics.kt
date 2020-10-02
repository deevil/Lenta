package com.lenta.shared.analytics

import androidx.annotation.WorkerThread
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.lenta.shared.utilities.Logg
import com.mobrun.plugin.api.HyperHive
import java.util.*
import javax.inject.Inject

class FmpAnalytics @Inject constructor(val hyperHive: HyperHive) : IAnalytics {

    private var isEnabledLogs = false

    override fun enableLogs(enable: Boolean) {
        this.isEnabledLogs = enable
    }


    override fun init() {
        //hyperHive.loggingAPI.initAutoSendingLogs()
    }


    override fun logTrace(tag: String?, message: String) {
        if (!isEnabledLogs) {
            return
        }
        val logMessage = "logTrace: $tag, message: $message"
        Logg.d { logMessage }
        FirebaseCrashlytics.getInstance().log("${Date()} - $logMessage")
        //logDao.insert(LogMessage(Date(), InfoLevel.INFO, message))
        //enableLogsFuncDisableLogs(tag, message, hyperHive.loggingAPI::logTrace)
    }


    override fun logError(tag: String?, message: String) {
        if (!isEnabledLogs) {
            return
        }
        val logMessage = "logError: $tag, message: $message"
        Logg.e { logMessage }
        FirebaseCrashlytics.getInstance().log("${Date()} - $logMessage")
        //logDao.insert(LogMessage(Date(), InfoLevel.ERROR, message))
        //enableLogsFuncDisableLogs(tag, message, hyperHive.loggingAPI::logWarning)
    }

    override fun logFatal(tag: String?, message: String) {
        if (!isEnabledLogs) {
            return
        }
        val logMessage = "logFatal: $tag, message: $message"
        Logg.d { logMessage }
        FirebaseCrashlytics.getInstance().log("${Date()} - $logMessage")
        //logDao.insert(LogMessage(Date(), InfoLevel.FATAL, message))
        //enableLogsFuncDisableLogs(tag, message, hyperHive.loggingAPI::logFatal)
    }


    override fun cleanLogs() {
        if (!isEnabledLogs) {
            return
        }
        hyperHive.databaseAPI.query(hyperHive.loggingAPI.loggingBasePath, "DELETE FROM hyperhive_journal").execute()
    }

    @WorkerThread
    override fun sendLogs() {
        //hyperHive.loggingAPI.sendLogs().execute()
    }

    /*private fun enableLogsFuncDisableLogs(tag: String?, message: String, funcSendLogs: KFunction1<@ParameterName(name = "message") String, Unit>) {
        Logg.d { "logTrace: $tag, message: $message" }
        enableFmpLogs()
        funcSendLogs("${tag.orEmpty()}\n$message")
        disableFmpLogs()
    }*/

    private fun enableFmpLogs() {
        hyperHive.loggingAPI.setLogLevel(4)
    }

    private fun disableFmpLogs() {
        hyperHive.loggingAPI.setLogLevel(10)
    }


}