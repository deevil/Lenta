package com.lenta.bp16.model.pojo


data class Raw(
        val name: String,
        val planned: Double,
        var quantity: Double = 0.0
) {
}