package com.lenta.bp9.features.goods_list

import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.TaskZBatchesDiscrepancies
import com.lenta.shared.utilities.databinding.Evenable

data class ListCountedItem(
        val number: Int,
        val name: String,
        val nameMaxLines: Int,
        val nameBatch: String,
        val visibilityNameBatch: Boolean,
        val countAcceptWithUom: String,
        val countRefusalWithUom: String,
        val isNotEdit: Boolean,
        val productInfo: TaskProductInfo?,
        val zBatchDiscrepancies: TaskZBatchesDiscrepancies?,
        val even: Boolean
) : Evenable {
    override fun isEven() = even

}

data class ListWithoutBarcodeItem(
        val number: Int,
        val name: String,
        val nameMaxLines: Int,
        val nameBatch: String,
        val visibilityNameBatch: Boolean,
        val productInfo: TaskProductInfo?,
        val even: Boolean
) : Evenable {
    override fun isEven() = even

}

data class ListShipmentPPItem(
        val number: Int,
        val name: String,
        val countWithUom: String,
        val productInfo: TaskProductInfo?,
        val even: Boolean
) : Evenable {
    override fun isEven() = even

}