package com.lenta.shared.exception

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.ContextCompat
import com.lenta.shared.R
import javax.inject.Inject

class CoreFailureInterpreter
@Inject constructor(val context: Context) : IFailureInterpreter {

    @SuppressLint("StringFormatMatches")
    override fun getFailureDescription(failure: Failure): FailureDescription {
        return when (failure) {
            Failure.ServerError -> FailureDescription(message = context.getString(R.string.error_server))

            Failure.TimeOutError -> FailureDescription(
                    iconRes = R.drawable.ic_warning_yellow_80dp,
                    message = context.getString(R.string.time_out_error)
            )

            Failure.WeighingError -> FailureDescription(message = context.getString(R.string.error_connect_weight_equipment))

            Failure.AuthError -> FailureDescription(
                    message = context.getString(R.string.error_auth),
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    iconRes = R.drawable.ic_warning_red_80dp
            )

            Failure.NetworkConnection -> FailureDescription(message = context.getString(R.string.error_network))

            Failure.FileReadingError -> FailureDescription(message = context.getString(R.string.file_reding_error))

            Failure.GoodNotFound -> FailureDescription(
                    message = context.getString(R.string.good_not_found),
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    iconRes = R.drawable.ic_warning_red_80dp
            )

            Failure.InvalidProductForTask -> FailureDescription(
                    message = context.getString(R.string.good_is_not_part_of_task),
                    iconRes = R.drawable.ic_warning_red_80dp
            )

            Failure.NotValidEnterNumber -> FailureDescription(message = context.getString(R.string.not_valid_format_ean))

            is NotFoundAppUpdateFileError -> FailureDescription(
                    message = context.getString(R.string.not_found_app_file_update, "${failure.codeVersion}"),
                    iconRes = R.drawable.ic_warning_red_80dp
            )

            is Failure.SapError -> FailureDescription(
                    message = failure.message,
                    iconRes = R.drawable.ic_warning_red_80dp
            )

            is Failure.DbError -> FailureDescription(message = context.getString(R.string.db_error))
            is Failure.MessageFailure -> {
                val message = failure.messageResId?.let(context::getString) ?: failure.message
                ?: context.getString(R.string.error_unknown)
                FailureDescription(message = message, iconRes = R.drawable.ic_warning_yellow_80dp)
            }

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