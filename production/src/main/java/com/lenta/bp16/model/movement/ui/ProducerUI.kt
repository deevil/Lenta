package com.lenta.bp16.model.movement.ui

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProducerUI(
        val producerName: List<String>,
        val producerCode: List<String>
) : Parcelable