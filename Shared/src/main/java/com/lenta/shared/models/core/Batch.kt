package com.lenta.shared.models.core

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Batch(
        val batchNumber: String,
        val count: String? = null,
        val dateOfProduction: Date? = null,
        val dateOfExpiration: Date? = null,
        val manufacturer: Manufacturer? = null
) : Parcelable {

    companion object {
        fun setBottlingDate(batch: Batch, dateOfProduction: Date, dateOfExpiration: Date? = null): Batch {
            return Batch(batch.batchNumber, batch.count, dateOfProduction, dateOfExpiration, batch.manufacturer)
        }
    }
}