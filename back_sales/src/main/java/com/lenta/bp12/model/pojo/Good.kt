package com.lenta.bp12.model.pojo

import com.lenta.bp12.model.GoodType
import com.lenta.bp12.model.QuantityType
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.Uom

data class Good(
        val ean: String,
        val material: String,
        val name: String,
        val quantity: Double = 0.0,
        //val quantityType: QuantityType,
        val innerQuantity: Double,
        val units: Uom,
        val orderUnits: Uom,
        val type: GoodType,
        val isAlcohol: Boolean,
        val isExcise: Boolean,
        val providers: List<ProviderItem>,
        val producers: List<ProducerItem>,
        val matrix: MatrixType,
        val section: String
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

    fun getPreparedProducerList(): List<String> {
        if (producers.isEmpty()) {
            return emptyList()
        }

        val list = producers.map { it.name }.toMutableList()
        if (list.size > 1) {
            list.add(0, "")
        }

        return list
    }

}