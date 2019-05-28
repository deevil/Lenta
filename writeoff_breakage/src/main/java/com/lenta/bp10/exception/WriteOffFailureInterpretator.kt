package com.lenta.bp10.exception

import android.content.Context
import com.lenta.bp10.R
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import javax.inject.Inject

class WriteOffFailureInterpretator @Inject constructor(
        private val context: Context,
        private val coreFailureInterpreter: IFailureInterpreter) : IWriteOffFailureInterpretator {
    override fun getFailureDescription(failure: Failure): String {
        coreFailureInterpreter.getFailureDescription(failure).let { coreMessage ->
            return if (coreMessage == context.getString(com.lenta.shared.R.string.error_unknown)) {
                getCustomFailureDescription(failure)
            } else coreMessage
        }

    }

    private fun getCustomFailureDescription(failure: Failure): String {
        return when (failure) {
            Failure.GoodNotFound -> context.getString(R.string.good_not_found)
            is Failure.SapError -> failure.message
            else -> context.getString(com.lenta.shared.R.string.error_unknown)
        }
    }
}

interface IWriteOffFailureInterpretator : IFailureInterpreter