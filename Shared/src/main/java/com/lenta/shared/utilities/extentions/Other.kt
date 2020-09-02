package com.lenta.shared.utilities.extentions

import androidx.lifecycle.LiveData

/**
 * This method non thread safe and take less memory for execution
 */
inline fun <reified T, reified R> R.unsafeLazy(noinline init: () -> T): Lazy<T> {
    return lazy(LazyThreadSafetyMode.NONE, init)
}

/**
 * Получение позиции liveData
 * */
fun <T : LiveData<Int>> T.getOrDefault(default: Int = 0): Int {
    return this.value ?: default
}

fun <T : LiveData<Int?>> T.getOrDefaultWithNull(default: Int = 0): Int {
    return this.value ?: default
}

/**
 * Получение элемента из liveData по индексу
 * */
fun <T : LiveData<List<String>>> T.getOrEmpty(index: Int): String {
    return this.value?.getOrNull(index).orEmpty()
}