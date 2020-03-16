package com.lenta.bp12.model.pojo

import com.lenta.bp12.model.GoodType
import com.lenta.shared.models.core.Uom

data class Good(
        val ean: String,
        val material: String,
        val name: String,
        val type: GoodType,
        val quantity: Double = 0.0,
        val units: Uom
)