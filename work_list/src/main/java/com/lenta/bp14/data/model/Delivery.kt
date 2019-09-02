package com.lenta.bp14.data.model

import com.lenta.shared.models.core.Uom
import java.util.*


data class Delivery(
        val id: Int,
        val good: Good,
        val status: DeliveryStatus,
        val additional: String, // ПП, РЦ, ...
        val quantity: Int,
        val uom: Uom = Uom.DEFAULT,
        val date: Date
) {
}

enum class DeliveryStatus() {
    ON_WAY,
    ORDERED
}