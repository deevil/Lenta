package com.lenta.shared.utilities.extentions

fun <T> MutableList<T>.removeItemFromListWithPredicate(
        predicate: (T) -> Boolean
): Boolean {
    this.map { it }.filter { stamp ->
        if (predicate(stamp)) {
            this.remove(stamp)
            return@filter true
        }
        return@filter false
    }.let {
        return it.isNotEmpty()
    }
}