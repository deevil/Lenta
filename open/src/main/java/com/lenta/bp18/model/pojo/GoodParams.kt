package com.lenta.bp18.model.pojo

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GoodParams(
        val ean: String,
        val weight: String,
        val material: String,
        val name: String,
        val batchNumber: String
): Parcelable