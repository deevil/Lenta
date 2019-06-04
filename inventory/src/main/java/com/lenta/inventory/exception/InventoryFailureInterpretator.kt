package com.lenta.inventory.exception

import android.content.Context
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import javax.inject.Inject

class InventoryFailureInterpretator @Inject constructor(
        private val context: Context,
        private val coreFailureInterpreter: IFailureInterpreter) : IInventoryFailureInterpretator {
    override fun getFailureDescription(failure: Failure): String {
        coreFailureInterpreter.getFailureDescription(failure).let { coreMessage ->
            return if (coreMessage == context.getString(com.lenta.shared.R.string.error_unknown)) {
                getCustomFailureDescription(failure)
            } else coreMessage
        }

    }

    private fun getCustomFailureDescription(failure: Failure): String {
        return when (failure) {
            is Failure.SapError -> failure.message
            else -> context.getString(com.lenta.shared.R.string.error_unknown)
        }
    }
}

interface IInventoryFailureInterpretator : IFailureInterpreter