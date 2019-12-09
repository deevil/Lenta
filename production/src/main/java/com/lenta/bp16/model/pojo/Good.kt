package com.lenta.bp16.model.pojo

import com.lenta.shared.models.core.Uom

data class Good(
        var isProcessed: Boolean = false,
        val material: String,
        val name: String,
        val units: Uom,
        var planned: Double,
        var total: Double = 0.0,
        var raws: MutableList<Raw> = mutableListOf(),
        var packs: MutableList<Pack> = mutableListOf()
) {

    fun getFactRawQuantity(): Double {
        return raws.map { it.quantity }.sum()
    }

    fun getNameWithMaterial(delimiter: String = " "): String {
        return "${material.takeLast(6)}$delimiter$name"
    }

}