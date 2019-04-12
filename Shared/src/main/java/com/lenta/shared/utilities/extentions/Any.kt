package com.lenta.shared.utilities.extentions


fun <T> Any.implementation(clazz: Class<T>): T? {
    return if (clazz.isInstance(clazz)) {
        @Suppress("UNCHECKED_CAST")
        this as T
    } else {
        return null
    }

}