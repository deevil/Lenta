package com.lenta.shared.utilities.extentions

fun <T> MutableList<T>.addItemToListWithPredicate(element: T, predicate: (T) -> Boolean): Boolean {
    this.none { predicate(it) }
            .takeIf { it }
            ?.also {
                this.add(element)
                return true
            }
    return false
}

fun <T> MutableList<T>.removeItemFromListWithPredicate(predicate: (T) -> Boolean): Boolean {
    this.map { it }.filter { element ->
        if (predicate(element)) {
            this.remove(element)
            return@filter true
        }
        return@filter false
    }.let {
        return it.isNotEmpty()
    }
}