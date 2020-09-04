package com.lenta.bp12.model.pojo

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.GoodKind
import com.lenta.bp12.model.MarkType
import com.lenta.bp12.request.pojo.ProducerInfo
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.sumList

/**
 * Родительский класс для всех товаров, Basket хранит именно его
 * Методы для взаимодействия со списками находятся в файле GoodExt:
 * @see com.lenta.bp12.model.pojo.extentions.addPosition
 * @see com.lenta.bp12.model.pojo.extentions.addPart
 * и тд
 * */
abstract class Good(
        var ean: String,
        val eans: List<String> = emptyList(),
        val material: String,
        val name: String,
        val kind: GoodKind,
        val section: String,
        val matrix: MatrixType,
        val volume: Double,
        val control: ControlType = ControlType.COMMON,

        val commonUnits: Uom,
        val innerUnits: Uom,
        val innerQuantity: Double,

        val producers: MutableList<ProducerInfo> = mutableListOf(),
        val positions: MutableList<Position> = mutableListOf(),
        val marks: MutableList<Mark> = mutableListOf(),
        val parts: MutableList<Part> = mutableListOf(),
        val markType: MarkType,
        val markTypeGroup: MarkTypeGroup?,
        val maxRetailPrice: String = ""
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

    private fun getPositionQuantity(): Double {
        return positions.map { it.quantity }.sumList()
    }

    fun getMarkQuantity(): Double {
        return marks.size.toDouble()
    }

    fun getPartQuantity(): Double {
        return parts.map { it.quantity }.sumList()
    }

    fun isEmpty(): Boolean {
        return positions.isEmpty() && marks.isEmpty() && parts.isEmpty()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Good

        if (material != other.material) return false

        return true
    }

    override fun hashCode(): Int {
        return material.hashCode()
    }

    override fun toString(): String {
        return "Good(ean='$ean', eans=$eans, material='$material', name='$name', kind=$kind, section='$section', matrix=$matrix, volume=$volume, control=$control, commonUnits=$commonUnits, innerUnits=$innerUnits, innerQuantity=$innerQuantity, producers=$producers, positions=$positions, marks=$marks, parts=$parts, markType=$markType, markTypeGroup=$markTypeGroup, maxRetailPrice='$maxRetailPrice')"
    }


}