package com.lenta.bp9.features.goods_list

import com.lenta.bp9.model.ReceivingProductInfo
import com.lenta.shared.utilities.databinding.Evenable

data class GoodsListCountedItem(
        val number: Int,
        val name: String,
        val quantity: String,
        val minus: String,
        val even: Boolean,
        val productInfo: ReceivingProductInfo
) : Evenable {
    override fun isEven() = even

}

data class GoodsListWithoutBarcodeItem(
        val number: Int,
        val name: String,
        val quantity: String,
        val even: Boolean,
        val productInfo: ReceivingProductInfo
) : Evenable {
    override fun isEven() = even

}