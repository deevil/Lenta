package com.lenta.bp12.model.pojo

data class Good(
        val ean: String,
        val material: String,
        val name: String,
        val quantity: Double = 0.0
)