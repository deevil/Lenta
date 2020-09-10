package com.lenta.bp16.model.ingredients

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class IngredientUI (
        val material: String,
        val name: String,
        val shelfLife: String
): Parcelable