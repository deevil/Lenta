package com.lenta.bp14.data.model

import com.lenta.shared.platform.constants.Constants
import java.text.SimpleDateFormat
import java.util.*


data class ShelfLife(
        val id: Int = 0,
        var shelfLife: Date?,
        var publicationDate: Date?,
        var quantity: Int = 0
) {

    fun getFormattedShelfLife(): String {
        return if (shelfLife != null) {
            SimpleDateFormat(Constants.DATE_FORMAT_ddmmyy, Locale.getDefault()).format(shelfLife)
        } else ""
    }

    fun getFormattedPublicationDate(): String {
        return if (publicationDate != null) {
            SimpleDateFormat(Constants.DATE_FORMAT_ddmmyy, Locale.getDefault()).format(publicationDate)
        } else ""
    }

}