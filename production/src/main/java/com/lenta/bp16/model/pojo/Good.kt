package com.lenta.bp16.model.pojo

import com.lenta.shared.models.core.Uom

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
        return packs.map { it.quantity }.sum()
    }

    fun getNameWithMaterial(delimiter: String = " "): String {
        return "${material.takeLast(6)}$delimiter$name"
    }

    /*fun isExistNotDefectPack(): Boolean {
        return packs.any { it.category != null || it.defect != null }
    }*/

}