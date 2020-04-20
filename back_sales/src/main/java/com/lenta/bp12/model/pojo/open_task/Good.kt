package com.lenta.bp12.model.pojo.open_task

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.GoodKind
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.model.pojo.Part
import com.lenta.bp12.model.pojo.Position
import com.lenta.bp12.request.pojo.ProducerInfo
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.utilities.extentions.sumList
import com.lenta.shared.utilities.extentions.sumWith

data class Good(
        val ean: String,
        val material: String,
        val name: String,
        val units: Uom = Uom.ST,
        val kind: GoodKind,
        val type: String = "",
        val control: ControlType = ControlType.COMMON,
        val section: String,
        val matrix: MatrixType,
        var positions: MutableList<Position> = mutableListOf(),

        var isFullData: Boolean = false,

        val innerQuantity: Double = 0.0,
        val orderUnits: Uom= Uom.ST,
        val providers: MutableList<ProviderInfo> = mutableListOf(), // ?
        val producers: MutableList<ProducerInfo> = mutableListOf(),
        val marks: MutableList<Mark> = mutableListOf(),
        val parts: MutableList<Part> = mutableListOf()
) {

    fun getNameWithMaterial(delimiter: String = " "): String {
        return "${material.takeLast(6)}$delimiter$name"
    }

    fun isBox(): Boolean {
        return innerQuantity > 1 // По умолчанию всегда 1.0
    }

    fun addPosition(quantity: Double, provider: ProviderInfo?) {
        val position = positions.find { it.provider?.code == provider?.code }
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
        return positions.map { it.quantity }.sumList()
    }

    fun getQuantityByProvider(provider: ProviderInfo?): Double {
        return positions.filter { it.provider?.code == provider?.code }.map { it.quantity }.sumList()
    }

    fun deletePositions(positionList: List<Position>) {
        positionList.forEach { position ->
            positions.remove(position)
        }
    }

    fun isSameMaterial(material: String): Boolean {
        return this.material.takeLast(6) == material.takeLast(6)
    }

}