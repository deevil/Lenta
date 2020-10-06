package com.lenta.bp15.model.pojo

import com.lenta.bp15.features.discrepancy_list.ItemDiscrepancyUi
import com.lenta.bp15.features.good_list.ItemGoodUi
import com.lenta.bp15.model.enum.ShoesMarkType
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.Uom

data class Good(
        val material: String,
        var name: String = "",
        var matrix: MatrixType = MatrixType.Unknown,
        var section: String = "",
        val planQuantity: Int,
        val markType: ShoesMarkType,
        var marks: Map<String, Mark> = emptyMap()
) {

    fun getShortMaterialWithName(): String {
        return "${material.takeLast(6)} $name"
    }

    fun getProcessedMarksCount(): Int {
        return marks.filter { it.value.isScan }.size
    }

    fun getUnprocessedMarksCount(): Int {
        return marks.filter { !it.value.isScan }.size
    }

    fun isExistProcessedMarks(): Boolean {
        return marks.any { it.value.isScan }
    }

    fun isExistUnprocessedMarks(): Boolean {
        return marks.any { !it.value.isScan }
    }

    fun changeScanStatusFor(scannedMarks: List<String>) {
        scannedMarks.forEach { number ->
            marks.getValue(number).isScan = true
        }
    }

    fun convertToItemProcessingUi(index: Int): ItemGoodUi {
        return ItemGoodUi(
                position = "${index + 1}",
                material = material,
                name = getShortMaterialWithName(),
                quantity = "${getUnprocessedMarksCount()}"
        )
    }

    fun convertToItemProcessedUi(index: Int): ItemGoodUi {
        return ItemGoodUi(
                position = "${index + 1}",
                material = material,
                name = getShortMaterialWithName(),
                quantity = "${getProcessedMarksCount()}"
        )
    }

    fun convertToItemDiscrepancyUi(index: Int): ItemDiscrepancyUi {
        return ItemDiscrepancyUi(
                position = "${index + 1}",
                material = material,
                name = getShortMaterialWithName(),
                quantity = "? ${getUnprocessedMarksCount()} ${Uom.ST.name}"
        )
    }

    fun putAdditionalInfo(additionalInfo: GoodAdditionalInfo) {
        name = additionalInfo.name
        matrix = additionalInfo.matrix
        section = additionalInfo.section
    }

}