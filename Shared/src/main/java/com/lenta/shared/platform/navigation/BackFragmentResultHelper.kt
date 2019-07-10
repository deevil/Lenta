package com.lenta.shared.platform.navigation

import java.util.*

class BackFragmentResultHelper {

    private var weakHashMap: WeakHashMap<Int, () -> Unit> = WeakHashMap()

    fun setFuncForResult(func: () -> Unit): Int {
        return func.hashCode().apply {
            weakHashMap[this] = func
        }
    }

    fun getFuncAndClear(idCodeFunc: Int?): (() -> Unit)? {

        weakHashMap[idCodeFunc]?.let {
            weakHashMap.clear()
            return it
        }

        return null
    }


}