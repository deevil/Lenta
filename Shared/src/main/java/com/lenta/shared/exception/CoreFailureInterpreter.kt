package com.lenta.shared.exception

import android.content.Context
import com.lenta.shared.R
import javax.inject.Inject

class CoreFailureInterpreter
@Inject constructor(val context: Context) : IFailureInterpreter {
    override fun getFailureDescription(failure: Failure): String {
        return when (failure) {
            Failure.ServerError -> context.getString(R.string.error_server)
            Failure.AuthError -> context.getString(R.string.error_auth)
            Failure.NetworkConnection -> context.getString(R.string.error_network)
            Failure.NetworkConnection -> context.getString(R.string.error_network)
            Failure.GoodNotFound -> context.getString(R.string.good_not_found)
            Failure.NotValidEnterNumber -> context.getString(R.string.not_valid_format_ean)
            is Failure.SapError -> failure.message
            else -> context.getString(R.string.error_unknown)
        }

    }


}

interface IFailureInterpreter {
    fun getFailureDescription(failure: Failure): String
}

