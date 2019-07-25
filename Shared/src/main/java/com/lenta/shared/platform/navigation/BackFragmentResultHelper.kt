package com.lenta.shared.platform.navigation

import java.lang.ref.SoftReference

class BackFragmentResultHelper {

    private var referenceHashMap: MutableMap<Int, SoftReference<() -> Unit>> = mutableMapOf()

    fun setFuncForResult(func: () -> Unit): Int {
        return func.hashCode().apply {
            referenceHashMap[this] = SoftReference(func)
        }
    }

    fun getFuncAndClear(idCodeFunc: Int?): (() -> Unit)? {
        val reference = referenceHashMap[idCodeFunc]
        referenceHashMap.clear()
        return reference?.get()
    }


}