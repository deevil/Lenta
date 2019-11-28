package com.lenta.bp9.features.mercury_list

import com.lenta.bp9.model.task.revise.ProductVetDocumentRevise
import com.lenta.shared.utilities.databinding.Evenable

data class MercuryListItem(
        val number: Int,
        val name: String,
        val quantityWithUom: String,
        val isCheck: Boolean,
        val productVetDocument: ProductVetDocumentRevise,
        val even: Boolean
) : Evenable {
    override fun isEven() = even

}