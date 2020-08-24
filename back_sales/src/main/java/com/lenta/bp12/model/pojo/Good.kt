package com.lenta.bp12.model.pojo

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.GoodKind
import com.lenta.bp12.request.pojo.ProducerInfo
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.sumList

/**
 * Родительский класс для всех товаров, Basket хранит именно его
 * Методы для взаимодействия со списками:
 * @see com.lenta.bp12.model.pojo.extentions.GoodExt
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

        if (ean != other.ean) return false
        if (eans != other.eans) return false
        
        if (material != other.material) return false
        if (name != other.name) return false
        if (kind != other.kind) return false
        if (section != other.section) return false
        if (matrix != other.matrix) return false
        if (volume != other.volume) return false
        if (commonUnits != other.commonUnits) return false
        if (innerUnits != other.innerUnits) return false
        if (innerQuantity != other.innerQuantity) return false
        if (producers != other.producers) return false
        if (positions != other.positions) return false
        if (marks != other.marks) return false
        if (parts != other.parts) return false
        if (ean != other.ean) return false

        return true
    }

    override fun hashCode(): Int {
        var result = eans.hashCode()
        result = 31 * result + material.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + kind.hashCode()
        result = 31 * result + section.hashCode()
        result = 31 * result + matrix.hashCode()
        result = 31 * result + volume.hashCode()
        result = 31 * result + commonUnits.hashCode()
        result = 31 * result + innerUnits.hashCode()
        result = 31 * result + innerQuantity.hashCode()
        result = 31 * result + producers.hashCode()
        result = 31 * result + positions.hashCode()
        result = 31 * result + marks.hashCode()
        result = 31 * result + parts.hashCode()
        result = 31 * result + ean.hashCode()
        return result
    }

    override fun toString(): String {
        return "Good(ean='$ean', eans=$eans, material='$material', name='$name', kind=$kind, section='$section', matrix=$matrix, volume=$volume, control=$control, commonUnits=$commonUnits, innerUnits=$innerUnits, innerQuantity=$innerQuantity, producers=$producers, positions=$positions, marks=$marks, parts=$parts)"
    }
}