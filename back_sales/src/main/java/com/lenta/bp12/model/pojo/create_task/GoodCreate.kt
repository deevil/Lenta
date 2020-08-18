package com.lenta.bp12.model.pojo.create_task

import com.lenta.bp12.model.ControlType
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

data class GoodCreate(
        var ean: String,
        val eans: List<String>,
        val material: String,
        val name: String,
        val kind: GoodKind,
        val type: String,
        val control: ControlType = ControlType.COMMON,
        val section: String,
        val matrix: MatrixType,
        val volume: Double,

        val commonUnits: Uom = Uom.ST,
        val innerUnits: Uom = Uom.ST,
        val innerQuantity: Double = 0.0,

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
        return commonUnits != innerUnits
    }

    fun getConvertingInfo(): String {
        return if (isDifferentUnits()) " (${commonUnits.name} = ${innerQuantity.dropZeros()} ${innerUnits.name})" else ""
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
        val markQuantity = marks.filter { it.providerCode == providerCode }.size.toDouble()
        val partQuantity = parts.filter { it.providerCode == providerCode }.map { it.quantity }.sumList()

        return positionQuantity.sumWith(markQuantity).sumWith(partQuantity)
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
        parts.find { it.providerCode == part.providerCode && it.producerCode == part.producerCode && it.date == part.date }?.let { found ->
            found.quantity = found.quantity.sumWith(part.quantity)
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

    fun removePartsByBasketNumber(basketIndex: Int) {
        parts.removeAll { it.basketNumber == basketIndex }
    }

    fun removeMarksByBasketIndex(basketIndex: Int) {
        marks.removeAll { it.basketNumber == basketIndex }
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GoodCreate

        if (material != other.material) return false

        return true
    }

    override fun hashCode(): Int {
        return material.hashCode()
    }


    val isAlco: Boolean
        get() = kind != GoodKind.COMMON

    val isExcise: Boolean
        get() = kind == GoodKind.EXCISE

    val isNotExcise: Boolean
        get() = kind == GoodKind.ALCOHOL

    val isUsual: Boolean
        get() = kind == GoodKind.COMMON


}