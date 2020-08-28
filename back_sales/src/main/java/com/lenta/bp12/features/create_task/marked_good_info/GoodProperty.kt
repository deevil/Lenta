package com.lenta.bp12.features.create_task.marked_good_info

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GoodProperty(
        val gtin: String,
        val property: String,
        val value: String
) : Parcelable