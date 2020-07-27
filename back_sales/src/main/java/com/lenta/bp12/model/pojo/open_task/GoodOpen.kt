package com.lenta.bp12.model.pojo.open_task

import com.lenta.bp12.model.GoodKind
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.model.pojo.Part
import com.lenta.bp12.model.pojo.Position
import com.lenta.bp12.request.pojo.ProducerInfo
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.sumList
import com.lenta.shared.utilities.extentions.sumWith

data class GoodOpen(
        var ean: String,
        val allGoodEans: List<String> = emptyList(),
        val material: String,
        val name: String,
        val kind: GoodKind,
        val section: String,
        val matrix: MatrixType,

        val planQuantity: Double = 0.0,
        val factQuantity: Double = 0.0,

        val commonUnits: Uom,
        val innerUnits: Uom,
        val innerQuantity: Double,

        var isCounted: Boolean = false,
        var isDeleted: Boolean = false,
        var isMissing: Boolean = false,

        val provider: ProviderInfo,
        val producers: List<ProducerInfo> = emptyList(),

        val positions: MutableList<Position> = mutableListOf(),
        val marks: MutableList<Mark> = mutableListOf(),
        val parts: MutableList<Part> = mutableListOf()
) {

    fun getNameWithMaterial(delimiter: String = " "): String {
        return "${material.takeLast(6)}$delimiter$name"
    }

    fun isDifferentUnits(): Boolean {
        return commonUnits != innerUnits
    }

    fun getConvertingInfo(): String {
        return if (isDifferentUnits()) " (${commonUnits.name} = ${innerQuantity.dropZeros()} ${innerUnits.name})" else ""
    }

    fun getQuantity(): Double {
        return factQuantity.takeIf { it > 0.0 } ?: getTotalQuantity()
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

    fun addPosition(position: Position) {
        positions.find { it.provider.code == position.provider.code }?.let { found ->
            found.quantity = found.quantity.sumWith(position.quantity)
        } ?: positions.add(position)
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

    fun isEmpty(): Boolean {
        return positions.isEmpty() && marks.isEmpty() && parts.isEmpty()
    }

}