package com.lenta.shared.utilities

import android.os.Build
import android.util.Log
import android.util.Log.*

object Logger {

    private val DEFAULT_MESSSAGE = "ಠ_ಠ"

    var MIN_LOG_LEVEL = VERBOSE

    private val emptyMessage: () -> String = { DEFAULT_MESSSAGE }

    fun v(tag: String = "", message: () -> String = emptyMessage) = doForLevel(VERBOSE) {
        Log.v(getTag(tag), createNotEmptyMessage(message()))
    }

    fun v(tag: String = "", message: () -> String = emptyMessage, exception: Exception) =
        doForLevel(VERBOSE) {
            Log.v(getTag(tag), createNotEmptyMessage(message()), exception)
        }

    fun d(tag: String = "", message: () -> String = emptyMessage) = doForLevel(DEBUG) {
        Log.d(getTag(tag), createNotEmptyMessage(message()))
    }

    fun d(tag: String = "", message: () -> String = emptyMessage, exception: Exception) =
        doForLevel(DEBUG) {
            Log.d(getTag(tag), createNotEmptyMessage(message()), exception)
        }

    fun i(tag: String = "", message: () -> String = emptyMessage) = doForLevel(INFO) {
        Log.i(getTag(tag), createNotEmptyMessage(message()))
    }

    fun i(tag: String = "", message: () -> String = emptyMessage, exception: Exception) =
        doForLevel(INFO) {
            Log.i(getTag(tag), createNotEmptyMessage(message()), exception)
        }

    fun w(tag: String = "", message: () -> String = emptyMessage) = doForLevel(WARN) {
        Log.w(getTag(tag), createNotEmptyMessage(message()))
    }

    fun w(tag: String = "", message: () -> String = emptyMessage, exception: Exception) =
        doForLevel(WARN) {
            Log.w(getTag(tag), createNotEmptyMessage(message()), exception)
        }

    fun e(tag: String = "", message: () -> String = emptyMessage) = doForLevel(ERROR) {
        Log.e(getTag(tag), createNotEmptyMessage(message()))
    }

    fun e(tag: String = "", message: () -> String = emptyMessage, exception: Exception) =
        doForLevel(ERROR) {
            Log.e(getTag(tag), createNotEmptyMessage(message()), exception)
        }

    fun wtf(tag: String = "", message: () -> String = emptyMessage) = doForLevel(ASSERT) {
        Log.wtf(getTag(tag), createNotEmptyMessage(message()))
    }

    fun wtf(tag: String = "", message: () -> String = emptyMessage, exception: Exception) =
        doForLevel(ASSERT) {
            Log.wtf(getTag(tag), createNotEmptyMessage(message()), exception)
        }

    private inline fun doForLevel(logLevel: Int, action: () -> Unit) {
        if (logLevel >= MIN_LOG_LEVEL) {
            action()
        }
    }

    private fun getTag(tag: String): String {
        return if (tag.isEmpty()) callerTag() else tag
    }

    private fun createNotEmptyMessage(message: String): String {
        if (message.isEmpty()) {
            return DEFAULT_MESSSAGE
        }
        return message
    }

    /**
     * @return The class name for the calling class as a String.
     */
    private fun callerTag(): String {

        val callStackIndex = 4
        val maxTagLength = 23

        val stackTrace = Throwable().stackTrace
        val callerElement = stackTrace[callStackIndex]

        val tag =
            "(${callerElement.fileName}:${callerElement.lineNumber}) - ${callerElement.methodName}"

        // Tag length limit was removed in API 24.
        return if (tag.length <= maxTagLength || Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tag
        } else tag.substring(0, maxTagLength)
    }

}

