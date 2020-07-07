package com.lenta.movement.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CargoUnit(
        val number : String,
        val eoList: List<ProcessingUnit>
) : Parcelable