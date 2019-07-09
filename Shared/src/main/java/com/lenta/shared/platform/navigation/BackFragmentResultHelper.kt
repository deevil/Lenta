package com.lenta.shared.platform.navigation

import java.lang.ref.Reference
import java.lang.ref.WeakReference

class BackFragmentResultHelper {

    private var funcForResultRef: Reference<(() -> Unit)?> = WeakReference(null)
    private var idCodeFunc: Int? = null

    fun setFuncForResult(func: () -> Unit): Int {
        funcForResultRef = WeakReference(func)
        return func.hashCode().apply {
            idCodeFunc = this
        }
    }

    fun getFuncAndClear(idCodeFunc: Int?): (() -> Unit)? {
        if (idCodeFunc == null && idCodeFunc != this.idCodeFunc) {
            return null
        }
        funcForResultRef.get().let {
            funcForResultRef.clear()
            return it
        }
    }


}