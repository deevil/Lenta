package com.lenta.bp12.model.pojo

import java.util.*

data class Part(
        var number: String,
        var material: String,
        var providerCode: String,
        var producerCode: String,
        var date: Date
) {
    var quantity: Double = 0.0
    var basketNumber: Int = 0
}