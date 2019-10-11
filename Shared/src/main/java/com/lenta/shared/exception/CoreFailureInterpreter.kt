package com.lenta.shared.exception

import android.content.Context
import androidx.core.content.ContextCompat
import com.lenta.shared.R
import javax.inject.Inject

class CoreFailureInterpreter
@Inject constructor(val context: Context) : IFailureInterpreter {
    override fun getFailureDescription(failure: Failure): FailureDescription {
        return when (failure) {
            Failure.ServerError -> FailureDescription(message = context.getString(R.string.error_server))

            Failure.AuthError -> FailureDescription(message = context.getString(R.string.error_auth),
                    iconRes = R.drawable.is_warning_red_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning))

            Failure.NetworkConnection -> FailureDescription(message = context.getString(R.string.error_network))

            Failure.GoodNotFound -> FailureDescription(message = context.getString(R.string.good_not_found),
                    iconRes = R.drawable.is_warning_red_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning))

            Failure.InvalidProductForTask -> FailureDescription(message = context.getString(R.string.good_is_not_part_of_task),
                    iconRes = R.drawable.is_warning_red_80dp)

            Failure.NotValidEnterNumber -> FailureDescription(message = context.getString(R.string.not_valid_format_ean))

            is Failure.SapError -> FailureDescription(
                    iconRes = R.drawable.is_warning_yellow_80dp,
                    message = failure.message)

            is Failure.DbError -> FailureDescription(message = context.getString(R.string.db_error))

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