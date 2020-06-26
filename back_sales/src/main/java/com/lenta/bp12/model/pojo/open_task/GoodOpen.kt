package com.lenta.bp12.model.pojo.open_task

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

data class GoodOpen(
        val ean: String,
        val material: String,
        val name: String,
        val section: String,
        val matrix: MatrixType,
        val type: GoodType,

        var planQuantity: Double = 0.0,
        var factQuantity: Double = 0.0,
        val innerQuantity: Double,
        val units: Uom,

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
        parts.find { it.providerCode == part.providerCode && it.producerCode == part.producerCode && it.date == part.date}?.let { foundPart ->
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








    /*fun replacePosition(quantity: Double, provider: ProviderInfo, category: Category, date: String?) {
        val position = positions.find { it.provider.code == provider.code && it.date == date }

        if (position != null) {
            positions.remove(position)
        }

        positions.add(0, Position(
                quantity = quantity,
                provider = provider,
                category = category,
                date = date,
                isCounted = true
        ))
    }*/

    /*fun deletePositions(positionList: List<Position>) {
        positionList.forEach { position ->
            positions.remove(position)
        }
    }

    fun isSameMaterial(material: String): Boolean {
        return this.material.takeLast(6) == material.takeLast(6)
    }

    fun markGoodAsDeleted(providerCode: String) {
        positions.find { it.provider.code == providerCode }?.let {
            it.quantity = 0.0
            it.isDelete = true
        }
    }

    fun markPositionUncounted(providerCode: String) {
        positions.find { it.provider.code == providerCode }?.let {
            it.quantity = 0.0
            it.isCounted = false
        }
    }

    fun markPositionMissing(providerCode: String) {
        quantity = 0.0
        isCounted = true
    }*/

}