package com.lenta.bp16.model.pojo


data class Raw(
        val orderNumber: String,
        val materialOsn: String,
        val name: String,
        val planned: Double,
        var totalQuantity: Double = 0.0
) {
}