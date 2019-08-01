package com.lenta.shared.utilities

import android.util.Base64
import com.lenta.shared.BuildConfig
import java.io.File


inline fun runIfDebug(function: () -> Unit) {
    if (BuildConfig.DEBUG) {
        function()
    }
}

inline fun runIfRelease(function: () -> Unit) {
    if (!BuildConfig.DEBUG) {
        function()
    }
}

fun getBaseAuth(login: String?, password: String?) : String {
    return "Basic " + "$login:$password".encode().dropLast(1)
}

fun String.encode(): String {
    return Base64.encodeToString(this.toByteArray(charset("UTF-8")), Base64.DEFAULT)
}

