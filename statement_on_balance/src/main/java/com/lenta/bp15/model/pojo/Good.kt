package com.lenta.bp15.model.pojo

import com.lenta.bp15.features.good_list.ItemGoodUi
import com.lenta.bp15.model.enum.ShoesMarkType
import com.lenta.shared.models.core.MatrixType

data class Good(
        val material: String,
        var name: String = "",
        var matrix: MatrixType = MatrixType.Unknown,
        var section: String = "",
        val planQuantity: Int,
        val markType: ShoesMarkType,
        val marks: List<Mark> = mutableListOf()
) {

    fun getNameWithShortMaterial(): String {
        return "${material.takeLast(6)} $name"
    }

    fun getQuantityToProcessing(): Int {
        return planQuantity - getProcessedMarks().size
    }

    fun isProcessed(): Boolean {
        return getProcessedMarks().size == planQuantity
    }

    fun isExistUnprocessedMarks(): Boolean {
        return marks.any { !it.isScan }
    }

    private fun getProcessedMarks(): List<Mark> {
        return marks.filter { it.isScan }
    }

    fun convertToItemGoodUi(index: Int): ItemGoodUi {
        return ItemGoodUi(
                position = "${index + 1}",
                material = material,
                name = getNameWithShortMaterial(),
                quantity = "${getQuantityToProcessing()}"
        )
    }

    fun putAdditionalInfo(additionalInfo: GoodAdditionalInfo) {
        name = additionalInfo.name
        matrix = additionalInfo.matrix
        section = additionalInfo.section
    }

}