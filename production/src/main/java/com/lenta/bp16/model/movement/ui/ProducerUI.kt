package com.lenta.bp16.model.movement.ui

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProducerUI(
        val producerName: String,
        val producerCode: String
) : Parcelable