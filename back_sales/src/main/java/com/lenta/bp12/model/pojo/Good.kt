package com.lenta.bp12.model.pojo

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.GoodKind
import com.lenta.bp12.request.pojo.ProducerInfo
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.utilities.extentions.sumWith

data class Good(
        val ean: String,
        val material: String,
        val name: String,
        val innerQuantity: Double,
        val units: Uom,
        val orderUnits: Uom,
        val kind: GoodKind,
        val type: String,
        val control: ControlType,
        val section: String,
        val matrix: MatrixType,
        val providers: MutableList<ProviderInfo>,
        val producers: MutableList<ProducerInfo>,
        val positions: MutableList<Position> = mutableListOf(),
        val marks: MutableList<Mark> = mutableListOf(),
        val parts: MutableList<Part> = mutableListOf()
) {

    fun getNameWithMaterial(delimiter: String = " "): String {
        return "${material.takeLast(6)}$delimiter$name"
    }

    fun isBox(): Boolean {
        return innerQuantity > 1
    }

    fun addPosition(quantity: Double, provider: ProviderInfo?) {
        val position = positions.find { it.provider == provider }
        val oldQuantity = position?.quantity

        if (position != null) {
            positions.remove(position)
        }

        positions.add(0, Position(
                quantity = quantity.sumWith(oldQuantity),
                provider = provider
        ))
    }

    fun getTotalQuantity(): Double {
        return positions.map { it.quantity }.sum()
    }

    fun getQuantityByProvider(provider: ProviderInfo?): Double {
        return positions.filter { it.provider == provider }.map { it.quantity }.sum()
    }

    fun deletePositions(positionList: List<Position>) {
        positionList.forEach { position ->
            positions.remove(position)
        }
    }

}