package com.lenta.bp10.platform

import com.lenta.bp10.BuildConfig

fun runIfDebug(function: () -> Unit) {
    if (BuildConfig.DEBUG) {
        function()
    }
}