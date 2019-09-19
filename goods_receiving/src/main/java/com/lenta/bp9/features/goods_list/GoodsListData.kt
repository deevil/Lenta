package com.lenta.bp9.features.goods_list

import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.shared.utilities.databinding.Evenable

data class GoodsListCountedItem(
        val number: Int,
        val name: String,
        val countAccept: Double,
        val countRefusal: Double,
        val even: Boolean,
        val productInfo: TaskProductInfo
) : Evenable {
    override fun isEven() = even

}

data class GoodsListWithoutBarcodeItem(
        val number: Int,
        val name: String,
        val even: Boolean,
        val productInfo: TaskProductInfo
) : Evenable {
    override fun isEven() = even

}