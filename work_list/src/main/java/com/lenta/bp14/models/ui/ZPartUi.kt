package com.lenta.bp14.models.ui

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ZPartUi(
        val index: String,
        val stock: String,
        val info: String,
        val largeInfo: String,
        val quantity: String
): Parcelable