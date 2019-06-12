package com.lenta.shared.exception

import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import com.lenta.shared.R
import javax.inject.Inject

class CoreFailureInterpreter
@Inject constructor(val context: Context) : IFailureInterpreter {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun getFailureDescription(failure: Failure): FailureDescription {
        return when (failure) {
            Failure.ServerError -> FailureDescription(message = context.getString(R.string.error_server))
            Failure.AuthError -> FailureDescription(message = context.getString(R.string.error_auth), iconRes = R.drawable.is_warning, textColor = context.getColor(R.color.color_text_dialogWarning))
            Failure.NetworkConnection -> FailureDescription(message = context.getString(R.string.error_network))
            Failure.GoodNotFound -> FailureDescription(message = context.getString(R.string.good_not_found), iconRes = R.drawable.is_warning, textColor = context.getColor(R.color.color_text_dialogWarning))
            Failure.NotValidEnterNumber -> FailureDescription(message = context.getString(R.string.not_valid_format_ean))
            is Failure.SapError -> FailureDescription(message = failure.message)
            else -> FailureDescription(message = context.getString(R.string.error_unknown))
        }

    }


}

interface IFailureInterpreter {
    fun getFailureDescription(failure: Failure): FailureDescription
}

data class FailureDescription(
        val message: String,
        val iconRes: Int = 0,
        val textColor: Int? = null
)