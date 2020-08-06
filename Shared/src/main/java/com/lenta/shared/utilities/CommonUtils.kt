package com.lenta.shared.utilities

import android.util.Base64
import com.lenta.shared.BuildConfig


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

/**
 * Helper nullability function
 */
inline fun <reified T> T?.orIfNull(input: () -> T): T {
    return this ?: input()
}

/**
 * Из строки получает Enum
 * */
inline fun <reified T : Enum<*>> enumValueOrNull(name: String): T? =
        T::class.java.enumConstants.firstOrNull { it.name == name }
