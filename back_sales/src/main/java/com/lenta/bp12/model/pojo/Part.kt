package com.lenta.bp12.model.pojo

import java.util.*

data class Part(
        val number: String,
        val material: String,
        val providerCode: String,
        val producerCode: String,
        val date: Date
) {
    var quantity: Double = 0.0
    var basketNumber: Int = 0
}