package com.lenta.bp12.model.pojo

import com.lenta.shared.models.core.Uom

data class Part(
        var number: String,
        var material: String,
        var quantity: Double,
        var units: Uom,
        var providerCode: String,
        var producerCode: String,
        var date: String
)