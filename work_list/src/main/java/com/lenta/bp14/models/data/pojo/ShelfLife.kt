package com.lenta.bp14.models.data.pojo

import com.lenta.shared.utilities.extentions.getFormattedDate
import java.util.*

data class ShelfLife(
        val id: Int = 0,
        var shelfLife: Date?,
        var publicationDate: Date?,
        var quantity: Int = 0
) {

    fun getFormattedShelfLife(): String {
        return shelfLife.getFormattedDate()
    }

    fun getFormattedPublicationDate(): String {
        return publicationDate.getFormattedDate()
    }

}