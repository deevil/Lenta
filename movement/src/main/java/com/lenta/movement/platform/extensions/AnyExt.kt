package com.lenta.movement.platform.extensions

import com.lenta.shared.utilities.extentions.implementationOf

inline fun <reified T> Any?.implementationOf(): T? {
    return implementationOf(T::class.java)
}