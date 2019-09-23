package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.revise.*

interface ITaskReviseDocumentsRepository {

    fun getDeliveryDocuments(): List<DeliveryDocumentRevise>
    fun updateDeliveryDocuments(documents: List<DeliveryDocumentRevise>)

    fun getProductDocuments(): List<DeliveryProductDocumentRevise>
    fun updateProductDocuments(documents: List<DeliveryProductDocumentRevise>)

    fun getImportABForms(): List<FormABImportRevise>
    fun updateImportABForms(forms: List<FormABImportRevise>)

    fun getRussianABForms(): List<FormABRussianRevise>
    fun updateRussianABForms(forms: List<FormABRussianRevise>)

    fun getProductBatches(): List<ProductBatchRevise>
    fun updateProductBatches(batches: List<ProductBatchRevise>)

    fun getSetComponents(): List<SetComponentRevise>
    fun updateSetComponents(components: List<SetComponentRevise>)

    fun getInvoiceInfo(): InvoiceRevise?
    fun updateInvoiceInfo(invoice: InvoiceRevise)

    fun clear()

    fun changeDeliveryDocumentStatus(documentID: String)
    fun changeInvoiceStatus()
}