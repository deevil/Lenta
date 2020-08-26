package com.lenta.bp16.model.pojo

import android.os.Parcelable
import com.lenta.bp16.model.movement.ui.ProducerUI
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GoodParams(
        val ean: String,
        val material: String,
        val weight: Double,
        val name: String,
        val zPart: Boolean,
        val uom: String,
        val umrez: String,
        val umren: String,
        val producers: List<ProducerUI>
) : Parcelable