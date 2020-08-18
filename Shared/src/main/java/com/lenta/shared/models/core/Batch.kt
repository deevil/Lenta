package com.lenta.shared.models.core

import java.util.*

data class Batch(
        val batchNumber: String,
        val manufacturer: Manufacturer? = null,
        val bottlingDate: Date? = null,
        val count: Int? = null
) {

    companion object {
        fun setBottlingDate(batch: Batch, bottlingDate: Date): Batch {
            return Batch(batch.batchNumber, batch.manufacturer, bottlingDate, batch.count)
        }
    }
}