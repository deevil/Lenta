package com.lenta.bp14.data.model

import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.constants.Constants
import java.text.SimpleDateFormat
import java.util.*


data class Delivery(
        val id: Int,
        val status: DeliveryStatus,
        val additional: String, // ПП, РЦ, ...
        val quantity: Int,
        val uom: Uom = Uom.KAR,
        val date: Date
) {

    fun getFormattedDate(): String {
        return SimpleDateFormat(Constants.DATE_FORMAT_ddmmyy, Locale.getDefault()).format(date)
    }

    fun getFormattedTime(): String {
        return SimpleDateFormat(Constants.TIME_FORMAT_HHmm, Locale.getDefault()).format(date)
    }

}

enum class DeliveryStatus(val description: String) {
    ON_WAY("В пути"),
    ORDERED("Заказан")
}