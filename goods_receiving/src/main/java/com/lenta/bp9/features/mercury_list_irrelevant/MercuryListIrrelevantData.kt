package com.lenta.bp9.features.mercury_list_irrelevant

import com.lenta.shared.utilities.databinding.Evenable

data class MercuryListIrrelevantItem(
        val number: Int,
        val name: String,
        val quantityWithUom: String,
        val even: Boolean
) : Evenable {
    override fun isEven() = even

}