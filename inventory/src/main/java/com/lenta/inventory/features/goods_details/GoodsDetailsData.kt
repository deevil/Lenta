package com.lenta.inventory.features.goods_details

import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.utilities.databinding.Evenable

data class GoodsDetailsCategoriesItem(
        val number: Int,
        val name: String,
        val quantity: String,
        val even: Boolean,
        val productInfo: ProductInfo
) : Evenable {
    override fun isEven() = even

}

data class GoodsDetailsStorageItem(
        val number: Int,
        val name: String,
        val quantity: String,
        val even: Boolean,
        val productInfo: ProductInfo
) : Evenable {
    override fun isEven() = even

}