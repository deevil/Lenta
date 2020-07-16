package com.lenta.bp12.model.pojo

import java.util.*

data class Part(
        var number: String,
        var material: String,
        var quantity: Double,
        var providerCode: String,
        var producerCode: String,
        var date: Date
)