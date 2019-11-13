package com.lenta.bp9.features.goods_list

import com.lenta.bp9.model.task.TaskBatchInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.shared.utilities.databinding.Evenable

data class ListCountedItem(
        val number: Int,
        val name: String,
        val uomName: String,
        val countAcceptWithUom: String,
        val countRefusalWithUom: String,
        val productInfo: TaskProductInfo?,
        val batchInfo: TaskBatchInfo?,
        val even: Boolean
) : Evenable {
    override fun isEven() = even

}

data class ListWithoutBarcodeItem(
        val number: Int,
        val name: String,
        val productInfo: TaskProductInfo?,
        val batchInfo: TaskBatchInfo?,
        val even: Boolean
) : Evenable {
    override fun isEven() = even

}