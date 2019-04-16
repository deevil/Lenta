package com.lenta.shared.exception

import android.content.Context
import com.lenta.shared.R
import javax.inject.Inject

class FailureInterpreter
@Inject constructor(val context: Context) : IFailureInterpreter {
    override fun getFailureDescription(failure: Failure): String {
        return when (failure) {
            Failure.ServerError -> context.getString(R.string.error_server)
            Failure.AuthError -> context.getString(R.string.error_auth)
            Failure.NetworkConnection -> context.getString(R.string.error_network)
            else -> context.getString(R.string.error_unknown)
        }

    }


}

interface IFailureInterpreter {
    fun getFailureDescription(failure: Failure): String
}

