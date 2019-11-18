package com.lenta.bp16.model.pojo

import com.lenta.shared.models.core.Uom


data class Good(
        val material: String,
        val name: String,
        val units: Uom,
        var planned: Double,
        var total: Double = 0.0
) {
}