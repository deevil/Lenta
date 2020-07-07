package com.lenta.bp12.model.pojo.create_task

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.GoodType
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.model.pojo.Part
import com.lenta.bp12.model.pojo.Position
import com.lenta.bp12.request.pojo.ProducerInfo
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.utilities.extentions.sumList
import com.lenta.shared.utilities.extentions.sumWith

data class GoodCreate(
        val ean: String,
        val material: String,
        val name: String,
        val type: GoodType,
        val matype: String = "",
        val control: ControlType = ControlType.COMMON,
        val section: String,
        val matrix: MatrixType,
        val commonUnits: Uom = Uom.ST,
        val convertingUnits: Uom = Uom.ST,
        val innerQuantity: Double = 0.0,
        val convertingInfo: String,

        val providers: MutableList<ProviderInfo> = mutableListOf(),
        val producers: MutableList<ProducerInfo> = mutableListOf(),

        var positions: MutableList<Position> = mutableListOf(),
        val marks: MutableList<Mark> = mutableListOf(),
        val parts: MutableList<Part> = mutableListOf()
) {

    fun getNameWithMaterial(delimiter: String = " "): String {
        return "${material.takeLast(6)}$delimiter$name"
    }

    fun isDifferentUnits(): Boolean {
        return commonUnits != convertingUnits
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

    fun getQuantityByProvider(providerCode: String?): Double {
        val positionQuantity = positions.filter { it.provider.code == providerCode }.map { it.quantity }.sumList()
        val partQuantity = parts.filter { it.providerCode == providerCode }.map { it.quantity }.sumList()

        return positionQuantity.sumWith(partQuantity)
    }

    fun addPosition(quantity: Double, provider: ProviderInfo) {
        val position = positions.find { it.provider.code == provider.code }
        val oldQuantity = position?.quantity

        if (position != null) {
            positions.remove(position)
        }

        positions.add(0, Position(
                quantity = quantity.sumWith(oldQuantity),
                provider = provider
        ))
    }

    fun addMark(mark: Mark) {
        if (marks.find { it.number == mark.number } == null) {
            marks.add(mark)
        }
    }

    fun addPart(part: Part) {
        parts.find { it.providerCode == part.providerCode && it.producerCode == part.producerCode && it.date == part.date }?.let { foundPart ->
            foundPart.quantity = foundPart.quantity.sumWith(part.quantity)
        } ?: parts.add(part)
    }

    fun removePositions(positionList: List<Position>) {
        positionList.forEach { position ->
            positions.remove(position)
        }
    }

    fun removeMarks(markList: List<Mark>) {
        markList.forEach { mark ->
            marks.remove(mark)
        }
    }

    fun removeParts(partList: List<Part>) {
        partList.forEach { part ->
            parts.remove(part)
        }
    }

    fun removeMark(number: String) {
        marks.find { it.number == number }?.let { mark ->
            marks.remove(mark)
        }
    }

    fun removeAllMark() {
        marks.clear()
    }

    fun removeAllPart() {
        parts.clear()
    }

    fun removePositionsByProvider(providerCode: String) {
        removePositions(positions.filter { it.provider.code == providerCode })
    }

    fun removeMarksByProvider(providerCode: String) {
        removeMarks(marks.filter { it.providerCode == providerCode })
    }

    fun removePartsByProvider(providerCode: String) {
        removeParts(parts.filter { it.providerCode == providerCode })
    }

    /*fun isSameMaterial(material: String): Boolean {
        return this.material.takeLast(6) == material.takeLast(6)
    }*/

}