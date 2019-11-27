package com.lenta.bp9.features.formed_docs

import com.lenta.shared.utilities.databinding.Evenable

data class FormedDocsItem(
        val number: Int,
        val name: String,
        val supplyNumber: String,
        val even: Boolean
) : Evenable {
    override fun isEven() = even

}