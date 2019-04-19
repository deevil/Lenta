package com.lenta.shared.models.core

import java.util.*

class Batch(val batchNumber: String, val manufacturer: Manufacturer, val bottlingDate: Date, val count: Int) {

    companion object {
        fun setBottlingDate(batch: Batch, bottlingDate: Date): Batch {
            return Batch(batch.batchNumber, batch.manufacturer, bottlingDate, batch.count)
        }
    }
}