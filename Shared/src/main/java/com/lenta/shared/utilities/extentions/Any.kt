package com.lenta.shared.utilities.extentions


fun <T> Any?.implementationOf(clazz: Class<T>): T? {
    return if (this != null && clazz.isAssignableFrom(clazz)) {
        @Suppress("UNCHECKED_CAST")
        this as T
    } else {
        return null
    }

}