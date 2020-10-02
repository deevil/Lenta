package com.lenta.bp14.models.ui

data class AdditionalInfoUi(
        val storagePlaces: String,
        val minStock: String,
        val inventory: String,
        val arrival: String,
        val commonPrice: String,
        val discountPrice: String,
        val promoName: String,
        val promoPeriod: String,
        val hasZParts: Boolean
)