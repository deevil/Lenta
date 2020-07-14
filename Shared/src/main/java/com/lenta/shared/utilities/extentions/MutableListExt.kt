package com.lenta.shared.utilities.extentions

fun <T> MutableList<T>.removeItemFromListWithPredicate(
        predicate: (T) -> Boolean
): Boolean {
    val localBad = this
    localBad.filter { stamp ->
        if (predicate(stamp)) {
            this.remove(stamp)
            return@filter true
        }
        return@filter false
    }.let {
        return it.isNotEmpty()
    }
}