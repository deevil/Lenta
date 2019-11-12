package com.lenta.bp9.features.editing_invoice

import com.lenta.bp9.model.task.revise.InvoiceContentEntry
import com.lenta.shared.models.core.Uom
import com.lenta.shared.utilities.databinding.Evenable

data class EditingInvoiceItem(
val number: Int,
val name: String,
var quantity: String,
val uom: Uom,
val invoiceContent: InvoiceContentEntry,
val even: Boolean
) : Evenable {
    override fun isEven() = even

}

data class NotesInvoiceItem(
        val number: Int,
        val lineNumber: String,
        var lineText: String,
        val even: Boolean
) : Evenable {
    override fun isEven() = even

}