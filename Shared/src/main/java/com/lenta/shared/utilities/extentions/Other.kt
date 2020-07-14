package com.lenta.shared.utilities.extentions

/**
 * This method non thread safe and take less memory for execution
 */
inline fun <reified T, reified R> R.unsafeLazy(noinline init: () -> T): Lazy<T> {
    return lazy(LazyThreadSafetyMode.NONE, init)
}