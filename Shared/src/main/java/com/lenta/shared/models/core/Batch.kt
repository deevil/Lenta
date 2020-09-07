package com.lenta.shared.models.core

import java.util.*

data class Batch(
        val batchNumber: String,
        val count: Double? = null,
        val dateOfProduction: Date? = null,
        val dateOfExpiration: Date? = null,
        val manufacturer: Manufacturer? = null
) {

    companion object {
        fun setBottlingDate(batch: Batch, dateOfProduction: Date, dateOfExpiration: Date? = null): Batch {
            return Batch(batch.batchNumber, batch.count, dateOfProduction, dateOfExpiration, batch.manufacturer)
        }
    }
}