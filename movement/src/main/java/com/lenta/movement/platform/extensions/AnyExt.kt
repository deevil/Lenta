package com.lenta.movement.platform.extensions

import com.lenta.shared.utilities.extentions.implementationOf

inline fun <reified T> Any?.implementationOf(): T? {
    return implementationOf(T::class.java)
}

/**
 * This method non thread safe and take less memory for execution
 */
inline fun <reified T, reified R> R.unsafeLazy(noinline init: () -> T): Lazy<T> =
        lazy(LazyThreadSafetyMode.NONE, init)