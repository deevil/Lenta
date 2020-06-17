package com.lenta.bp12.model.pojo.create_task

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.GoodType
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.model.pojo.Part
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
        val type: GoodType,
        val matype: String = "",
        val control: ControlType = ControlType.COMMON,
        val section: String,
        val matrix: MatrixType,
        var positions: MutableList<Position> = mutableListOf(),

        val innerQuantity: Double = 0.0,
        val orderUnits: Uom= Uom.ST,
        val providers: MutableList<ProviderInfo> = mutableListOf(),
        val producers: MutableList<ProducerInfo> = mutableListOf(),
        val marks: MutableList<Mark> = mutableListOf(),
        val parts: MutableList<Part> = mutableListOf()
) {

    fun getNameWithMaterial(delimiter: String = " "): String {
        return "${material.takeLast(6)}$delimiter$name"
    }

    fun addPosition(quantity: Double, provider: ProviderInfo?, date: String?) {
        val position = positions.find { it.provider?.code == provider?.code && it.date == date }
        val oldQuantity = position?.quantity

        if (position != null) {
            positions.remove(position)
        }

        positions.add(0, Position(
                quantity = quantity.sumWith(oldQuantity),
                provider = provider,
                date = date
        ))
    }

    fun getTotalQuantity(): Double {
        return getPositionQuantity() + getMarkQuantity() + getPartQuantity()
    }

    fun getPositionQuantity(): Double {
        return positions.map { it.quantity }.sumList()
    }

    fun getMarkQuantity(): Double {
        return marks.size.toDouble()
    }

    fun getPartQuantity(): Double {
        return parts.map { it.quantity }.sumList()
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

    fun addMark(mark: Mark) {
        if (marks.find { it.number == mark.number } == null) {
            marks.add(mark)
        }
    }

    fun addPart(part: Part) {
        if (parts.find { it.number == part.number } == null) {
            parts.add(part)
        }
    }

    fun removeMark(number: String) {
        marks.find { it.number == number }?.let { mark ->
            marks.remove(mark)
        }
    }

}