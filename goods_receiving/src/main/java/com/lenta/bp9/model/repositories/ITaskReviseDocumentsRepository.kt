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

    fun getTransportConditions(): List<TransportCondition>
    fun updateTransportCondition(conditions: List<TransportCondition>)

    fun clear()

    fun changeDeliveryDocumentStatus(documentID: String)
    fun changeProductDocumentStatus(documentID: String, matnr: String)
    fun approveAlcoDocument(matnr: String)
    fun approveImportForm(matnr: String, batchNumber: String, updatedGTDA: String?, updatedGTDB: String?)
    fun approveRussianForm(matnr: String, batchNumber: String)
    fun approveBatch(matnr: String, batchNumber: String)
    fun changeInvoiceStatus()
    fun changeTransportConditionStatus(id: String)
    fun changeTransportConditionValue(id: String, newValue: String)
}