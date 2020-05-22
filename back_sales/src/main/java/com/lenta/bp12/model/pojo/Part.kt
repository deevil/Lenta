package com.lenta.bp12.model.pojo

import com.lenta.shared.models.core.Uom

data class Part(
        var material: String,
        var producer: String,
        var productionDate: String,
        var quantity: Double,
        var units: Uom,
        var partNumber: String,
        var providerCode: String
)