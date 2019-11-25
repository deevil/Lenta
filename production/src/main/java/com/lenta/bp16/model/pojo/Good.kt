package com.lenta.bp16.model.pojo

import com.lenta.shared.models.core.Uom

data class Good(
        var isProcessed: Boolean = false,
        val material: String,
        val name: String,
        val units: Uom,
        var planned: Double,
        var total: Double = 0.0,
        var raws: List<Raw>? = null,
        var packs: List<Pack>? = null
) {

    fun getFactRawQuantity(): Double {
        return raws?.map { it.quantity }?.sum() ?: 0.0
    }

    fun getNameWithMaterial(): String {
        return "${material.takeLast(6)} $name"
    }

}