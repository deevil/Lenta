package com.lenta.inventory.features.goods_details_storage

import com.lenta.shared.models.core.EgaisStampVersion
import com.lenta.shared.utilities.databinding.Evenable

data class GoodsDetailsCategoriesItem(
        val number: Int,
        val name: String,
        val quantity: String,
        val even: Boolean,
        val egaisVersion: EgaisStampVersion
) : Evenable {
    override fun isEven() = even

}

data class GoodsDetailsStorageItem(
        val number: Int,
        val name: String,
        val quantity: String,
        val even: Boolean
) : Evenable {
    override fun isEven() = even

}

data class ComponentItem(
        val number: Int,
        val name: String,
        val quantity: String,
        val menge: String,
        val even: Boolean,
        val countSets: Double,
        val selectedPosition: Int,
        val setMaterialNumber: String
) : Evenable {
    override fun isEven() = even

}