package com.lenta.bp14.platform.resource

import android.content.Context
import com.lenta.bp14.R
import com.lenta.shared.utilities.extentions.unsafeLazy

import javax.inject.Inject

class ResourceManager @Inject constructor(val context: Context) : IResourceManager {
    override val serverConnectionError: String by unsafeLazy {
        context.getString(SERVER_CONNECTION_ERROR)
    }

    override val threeLinesPattern: String by unsafeLazy {
        context.getString(THREE_LINES_PATTERN)
    }

    override val fourLinesPattern: String by unsafeLazy {
        context.getString(FOUR_LINES_PATTERN)
    }

    override val prodDatePattern: String by unsafeLazy {
        context.getString(PROD_DATE_PATTERN)
    }

    override val prodDateLongPattern: String by unsafeLazy {
        context.getString(PROD_DATE_LONG_PATTERN)
    }

    override val expirDatePattern: String by unsafeLazy {
        context.getString(EXPIR_DATE_PATTERN)
    }

    override val expirDateLongPattern: String by unsafeLazy {
        context.getString(EXPIR_DATE_LONG_PATTERN)
    }

    override val datesDivider: String by unsafeLazy {
        context.getString(DATES_DIVIDER)
    }

    override val storageZPartsPattern: String by unsafeLazy {
        context.getString(STORAGE_Z_PARTS_PATTERN)
    }

    companion object {
        private const val SERVER_CONNECTION_ERROR = R.string.server_connection_error
        private const val THREE_LINES_PATTERN = R.string.three_lines_pattern
        private const val FOUR_LINES_PATTERN = R.string.four_lines_pattern
        private const val PROD_DATE_PATTERN = R.string.prod_date_pattern
        private const val PROD_DATE_LONG_PATTERN = R.string.prod_date_long_pattern
        private const val EXPIR_DATE_PATTERN = R.string.expir_date_pattern
        private const val EXPIR_DATE_LONG_PATTERN = R.string.expir_date_long_pattern
        private const val DATES_DIVIDER = R.string.dates_divider
        private const val STORAGE_Z_PARTS_PATTERN = R.string.storage_z_parts_pattern
    }
}

