package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskReviseDocumentsRepository
import com.lenta.bp9.model.task.revise.*

class MemoryTaskReviseDocumentsRepository : ITaskReviseDocumentsRepository {

    private val deliveryDocuments: ArrayList<DeliveryDocumentRevise> = ArrayList()
    private val productDocuments: ArrayList<DeliveryProductDocumentRevise> = ArrayList()
    private val importABForms: ArrayList<FormABImportRevise> = ArrayList()
    private val russianABForms: ArrayList<FormABRussianRevise> = ArrayList()
    private val productBatches: ArrayList<ProductBatchRevise> = ArrayList()
    private val setComponents: ArrayList<SetComponentRevise> = ArrayList()

    private var invoiceInfo: InvoiceRevise? = null

    override fun getDeliveryDocuments(): List<DeliveryDocumentRevise> {
        return deliveryDocuments.toList()
    }

    override fun updateDeliveryDocuments(documents: List<DeliveryDocumentRevise>) {
        deliveryDocuments.clear()
        deliveryDocuments.addAll(documents)
    }

    override fun getProductDocuments(): List<DeliveryProductDocumentRevise> {
        return productDocuments.toList()
    }

    override fun updateProductDocuments(documents: List<DeliveryProductDocumentRevise>) {
        productDocuments.clear()
        productDocuments.addAll(documents)
    }

    override fun getImportABForms(): List<FormABImportRevise> {
        return importABForms.toList()
    }

    override fun updateImportABForms(forms: List<FormABImportRevise>) {
        importABForms.clear()
        importABForms.addAll(forms)
    }

    override fun getRussianABForms(): List<FormABRussianRevise> {
        return russianABForms.toList()
    }

    override fun updateRussianABForms(forms: List<FormABRussianRevise>) {
        russianABForms.clear()
        russianABForms.addAll(forms)
    }

    override fun getProductBatches(): List<ProductBatchRevise> {
        return productBatches.toList()
    }

    override fun updateProductBatches(batches: List<ProductBatchRevise>) {
        productBatches.clear()
        productBatches.addAll(batches)
    }

    override fun getSetComponents(): List<SetComponentRevise> {
        return setComponents.toList()
    }

    override fun updateSetComponents(components: List<SetComponentRevise>) {
        setComponents.clear()
        setComponents.addAll(components)
    }

    override fun getInvoiceInfo(): InvoiceRevise? {
        return invoiceInfo
    }

    override fun updateInvoiceInfo(newInvoice: InvoiceRevise) {
        invoiceInfo = newInvoice
    }

    override fun changeDeliveryDocumentStatus(documentID: String) {
        val document = deliveryDocuments.findLast { it.documentID == documentID }
        document?.let { it.isCheck = !it.isCheck }
    }

    override fun changeInvoiceStatus() {
        val document = deliveryDocuments.findLast { it.documentType == DocumentType.Invoice }
        document?.let { it.isCheck = !it.isCheck }
    }

    override fun changeProductDocumentStatus(documentID: String, matnr: String) {
        val document = productDocuments.findLast { it.documentID == documentID && it.productNumber == matnr }
        document?.let { it.isCheck = !it.isCheck }
    }

    override fun approveAlcoDocument(matnr: String) {
        val document = productDocuments.findLast { it.productNumber == matnr && (it.documentType == ProductDocumentType.AlcoImport || it.documentType == ProductDocumentType.AlcoRus) }
        document?.let { it.isCheck = !it.isCheck }
    }

    override fun approveImportForm(matnr: String, batchNumber: String, updatedGTDA: String?, updatedGTDB: String?) {
        importABForms.findLast { it.productNumber == matnr && it.batchNumber == batchNumber }?.let { oldForm ->
            val newForm = oldForm.approvedCopy(updatedGTDA, updatedGTDB)
            importABForms.remove(oldForm)
            importABForms.add(newForm)
            approveBatch(matnr, batchNumber)
        }
    }

    override fun approveRussianForm(matnr: String, batchNumber: String) {
        russianABForms.findLast { it.productNumber == matnr && it.batchNumber == batchNumber }?.let { oldForm ->
            oldForm.isCheck = true
            approveBatch(matnr, batchNumber)
        }
    }

    override fun approveBatch(matnr: String, batchNumber: String) {
        productBatches.findLast { it.productNumber == matnr && it.batchNumber == batchNumber }?.let { batch ->
            batch.isCheck = true
            if (productBatches.findLast { it.productNumber == matnr && !it.isCheck } == null) {
                approveAlcoDocument(matnr)
            }
        }
    }

    override fun clear() {
        deliveryDocuments.clear()
        productDocuments.clear()
        importABForms.clear()
        russianABForms.clear()
        productBatches.clear()
        setComponents.clear()
        invoiceInfo = null
    }
}