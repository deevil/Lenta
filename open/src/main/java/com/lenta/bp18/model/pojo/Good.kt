package com.lenta.bp18.model.pojo

import com.lenta.shared.models.core.Uom
import com.lenta.shared.utilities.extentions.sumList

data class Good(
        var isProcessed: Boolean = false,
        val material: String,
        val name: String,
        val units: Uom,
        var arrived: Double,
        var raws: MutableList<Raw> = mutableListOf(),
        var packs: MutableList<Pack> = mutableListOf()
) {

    fun getPackedQuantity(): Double {
        return packs.map { it.quantity }.sumList()
    }

    fun getNotDefectQuantity(): Double {
        return packs.filter { it.isNotDefect() }.map { it.quantity }.sumList()
    }

    fun getDefectQuantity(): Double {
        return packs.filter { it.isDefect() }.map { it.quantity }.sumList()
    }

    fun getNameWithMaterial(delimiter: String = " "): String {
        return "${material.takeLast(6)}$delimiter$name"
    }

}