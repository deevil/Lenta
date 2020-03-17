package com.lenta.bp12.model.pojo

import com.lenta.bp12.model.GoodType
import com.lenta.shared.models.core.Uom

data class Good(
        val ean: String,
        val material: String,
        val name: String,
        val quantity: Double = 0.0,
        val innerQuantity: Double,
        val units: Uom,
        val type: GoodType,
        val isAlcohol: Boolean,
        val isExcise: Boolean,
        val providers: List<ProviderItem>
) {

    fun getNameWithMaterial(delimiter: String = " "): String {
        return "${material.takeLast(6)}$delimiter$name"
    }

    fun isBox(): Boolean {
        return innerQuantity > 0
    }

    fun getPreparedProviderList(): List<String> {
        if (providers.isEmpty()) {
            return emptyList()
        }

        val list = providers.map { it.name }.toMutableList()
        if (list.size > 1) {
            list.add(0, "")
        }

        return list
    }

}