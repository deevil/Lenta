package com.lenta.bp12.model.pojo

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.GoodKind
import com.lenta.bp12.model.MarkType
import com.lenta.bp12.request.pojo.ProducerInfo
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.sumList
import com.lenta.shared.utilities.extentions.sumWith
import com.lenta.shared.utilities.getDateFromString

/**
 * Родительский класс для всех товаров, Basket хранит именно его
 * Методы для взаимодействия со списками находятся в файле GoodExt:
 * @see com.lenta.bp12.model.pojo.extentions.addPosition
 * @see com.lenta.bp12.model.pojo.extentions.addPart
 * и тд
 * */
class Good(
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
        private val innerUnits: Uom,
        val innerQuantity: Double,

        val producers: MutableList<ProducerInfo> = mutableListOf(),
        val positions: MutableList<Position> = mutableListOf(),
        val marks: MutableList<Mark> = mutableListOf(),
        val parts: MutableList<Part> = mutableListOf(),
        val markType: MarkType = MarkType.UNKNOWN,
        val markTypeGroup: MarkTypeGroup? = null,
        val maxRetailPrice: String = "",

        val type: String,
        val providers: MutableList<ProviderInfo> = mutableListOf(),

        val planQuantity: Double = 0.0,
        val factQuantity: Double = 0.0,
        var isCounted: Boolean = false,
        var isDeleted: Boolean = false,
        val provider: ProviderInfo = ProviderInfo.getEmptyProvider()
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

    fun getPartQuantityByDateAndProducer(date: String, producerCode: String, quantityFromField: Double): Double? {
        return try {
            val dateFromString = getDateFromString(date, Constants.DATE_FORMAT_dd_mm_yyyy)
            parts.filter { part ->
                part.date == dateFromString && part.producerCode == producerCode
            }.sumByDouble { it.quantity }.sumWith(quantityFromField)
        } catch (e: RuntimeException) {
            null
        }
    }

    fun isEmpty(): Boolean {
        return positions.isEmpty() && marks.isEmpty() && parts.isEmpty()
    }

    fun isTobacco() = this.markType == MarkType.TOBACCO
    fun isTobaccoAndFoundGoodHasDifferentMrc(other: Good) =
            this.isTobacco() && maxRetailPrice.isNotEmpty() && maxRetailPrice != other.maxRetailPrice

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Good

        if (material != other.material) return false
        if (maxRetailPrice != other.maxRetailPrice) return false

        return true
    }

    override fun hashCode(): Int {
        val result = material.hashCode()
        return result * 31 * maxRetailPrice.hashCode()
    }

    override fun toString(): String {
        return "Good(ean='$ean', eans=$eans, material='$material', name='$name', kind=$kind, section='$section', matrix=$matrix, volume=$volume, control=$control, commonUnits=$commonUnits, innerUnits=$innerUnits, innerQuantity=$innerQuantity, producers=$producers, positions=$positions, marks=$marks, parts=$parts, markType=$markType, markTypeGroup=$markTypeGroup, maxRetailPrice='$maxRetailPrice')"
    }

}