package com.lenta.bp16.model.pojo

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GoodParams (
        val ean: String,
        val material: String,
        val name: String
): Parcelable