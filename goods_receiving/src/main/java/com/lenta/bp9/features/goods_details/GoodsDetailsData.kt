package com.lenta.bp9.features.goods_details

import com.lenta.bp9.model.task.TaskBatchesDiscrepancies
import com.lenta.bp9.model.task.TaskZBatchesDiscrepancies
import com.lenta.shared.utilities.databinding.Evenable

data class GoodsDetailsCategoriesItem(
        val number: Int,
        val name: String,
        val nameBatch: String,
        val visibilityNameBatch: Boolean,
        val quantityWithUom: String,
        val isNormDiscrepancies: Boolean,
        val typeDiscrepancies: String,
        val materialNumber: String,
        val batchDiscrepancies: TaskBatchesDiscrepancies?,
        val zBatchDiscrepancies: TaskZBatchesDiscrepancies?,
        val even: Boolean
) : Evenable {
    override fun isEven() = even

}

data class GoodsDetailsPropertiesItem(
        val number: Int,
        val ean: String,
        val properties: String,
        val value: String,
        val even: Boolean
) : Evenable {
    override fun isEven() = even

}