package com.lenta.bp14.models.data.pojo

import com.lenta.shared.models.core.Uom
import com.lenta.shared.utilities.extentions.getFormattedDate
import com.lenta.shared.utilities.extentions.getFormattedTime
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
        return date.getFormattedDate()
    }

    fun getFormattedTime(): String {
        return date.getFormattedTime()
    }

}

enum class DeliveryStatus(val description: String) {
    ON_WAY("В пути"),
    ORDERED("Заказан")
}