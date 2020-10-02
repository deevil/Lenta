package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskReviseDocumentsRepository
import com.lenta.bp9.model.task.TaskMercuryNotActual
import com.lenta.bp9.model.task.revise.*

class MemoryTaskReviseDocumentsRepository : ITaskReviseDocumentsRepository {

    private val deliveryDocuments: ArrayList<DeliveryDocumentRevise> = ArrayList()
    private val productDocuments: ArrayList<DeliveryProductDocumentRevise> = ArrayList()
    private val importABForms: ArrayList<FormABImportRevise> = ArrayList()
    private val russianABForms: ArrayList<FormABRussianRevise> = ArrayList()
    private val productBatches: ArrayList<ProductBatchRevise> = ArrayList()
    private val setComponents: ArrayList<SetComponentRevise> = ArrayList()
    private val transportConditions: ArrayList<TransportCondition> = ArrayList()
    private val productVetDocuments: ArrayList<ProductVetDocumentRevise> = ArrayList()
    private val complexDocuments: ArrayList<ComplexDocumentRevise> = ArrayList()
    private val mercuryNotActual: ArrayList<TaskMercuryNotActual> = ArrayList()

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

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun updateInvoiceInfo(newInvoice: InvoiceRevise) {
        invoiceInfo = newInvoice
    }

    override fun getTransportConditions(): List<TransportCondition> {
        return transportConditions.toList()
    }

    override fun updateTransportCondition(conditions: List<TransportCondition>) {
        transportConditions.clear()
        transportConditions.addAll(conditions)
    }

    override fun getProductVetDocuments(): List<ProductVetDocumentRevise> {
        return productVetDocuments
    }

    override fun updateProductVetDocuments(vetDocuments: List<ProductVetDocumentRevise>) {
        productVetDocuments.clear()
        productVetDocuments.addAll(vetDocuments)
    }

    override fun changeProductVetDocumentStatus(vetDocument: ProductVetDocumentRevise, status: Boolean) {
        val document = productVetDocuments.findLast { it.vetDocumentID == vetDocument.vetDocumentID && it.productNumber == vetDocument.productNumber }
        document?.let {
            it.isCheck = status
            it.isAttached = status
        }
    }

    override fun changeProductVetDocumentReconciliation(vetDocument: ProductVetDocumentRevise, reconciliationCheck: Boolean) {
        val document = productVetDocuments.findLast { it.vetDocumentID == vetDocument.vetDocumentID && it.productNumber == vetDocument.productNumber }
        document?.let {
            it.isCheck = reconciliationCheck
        }
    }

    override fun presenceUncoveredVadAllGoods() : Boolean {
        val presenceUncoveredVad = productDocuments.map {productDoc ->
            val vadVolume = productVetDocuments.filter {productVetDoc ->
                productVetDoc.productNumber == productDoc.productNumber && productVetDoc.isCheck
            }
            vadVolume.isNotEmpty()
        }.filter {
            it
        }

        return presenceUncoveredVad.isNullOrEmpty()
    }

    override fun presenceUncoveredVadSomeGoods(): Boolean {
        val presenceUncoveredVad = productDocuments.map {productDoc ->
            val vadVolume = productVetDocuments.filter {productVetDoc ->
                productVetDoc.productNumber == productDoc.productNumber
            }.sumByDouble {
                it.volume
            }
            vadVolume < productDoc.initialCount
        }.filter {
            it
        }

        return presenceUncoveredVad.size <= productDocuments.filter { it.documentType == ProductDocumentType.Mercury }.size && presenceUncoveredVad.isNotEmpty()
    }

    /**2.2. Устанавливать чек-бокс "Сверено" по веттовару, если в таблице ET_VET_CHK для текущего товара есть записи и для всех записей установлен признак FLG_CHECK
    (Т.е. сверенным считается товар у которого все привязанные ВСД сверены, суммарные количества в привязанных ВСД проверять на этом этапе не нужно)
    карточка trello 2460*/
    override fun setProductVetDocumentsReconciliation() {
        productDocuments.filter {
            it.documentType == ProductDocumentType.Mercury
        }.map {productDoc ->
            val countAttachedDocs = productVetDocuments.filter {productVetDoc ->
                productVetDoc.productNumber == productDoc.productNumber && productVetDoc.isAttached
            }.count()
            val countAttachedCheckDocs = productVetDocuments.filter {productVetDoc ->
                productVetDoc.productNumber == productDoc.productNumber && productVetDoc.isAttached && productVetDoc.isCheck
            }.count()
            val document = productDocuments.findLast { it.documentID == productDoc.documentID && it.productNumber == productDoc.productNumber }
            if (countAttachedDocs > 0 && countAttachedDocs == countAttachedCheckDocs) {
                document?.let { it.isCheck = true }
            }
        }
    }

    override fun getComplexDocuments(): List<ComplexDocumentRevise> {
        return complexDocuments
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun updateComplexDocuments(newComplexDocuments: List<ComplexDocumentRevise>) {
        complexDocuments.clear()
        complexDocuments.addAll(newComplexDocuments)
    }

    override fun setVerifiedCompositeDocument(documentID: String) {
        deliveryDocuments.findLast { it.documentID == documentID }?.let { it.isCheck = true }
        complexDocuments.filter {
            it.documentID == documentID
        }.map {
            it.isCheck = true
        }
    }

    override fun getMercuryNotActual(): List<TaskMercuryNotActual> {
        return mercuryNotActual
    }

    override fun updateMercuryNotActual(newMercuryNotActual: List<TaskMercuryNotActual>) {
        mercuryNotActual.clear()
        mercuryNotActual.addAll(newMercuryNotActual)
    }

    override fun changeDeliveryDocumentStatus(documentID: String) {
        val document = deliveryDocuments.findLast { it.documentID == documentID }
        document?.let { it.isCheck = !it.isCheck }
    }

    override fun changeInvoiceStatus(checked: Boolean) {
        val document = deliveryDocuments.findLast { it.documentType == DocumentType.Invoice }
        document?.let { it.isCheck = checked }
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

    override fun changeTransportConditionStatus(id: String) {
        transportConditions.findLast { it.conditionID == id }?.let { condition ->
            condition.isCheck = !condition.isCheck
        }
    }

    override fun changeTransportConditionValue(id: String, newValue: String) {
        transportConditions.findLast { it.conditionID == id }?.let { condition ->
            condition.value = newValue
            condition.isCheck = newValue.isNotEmpty()
        }
    }

    override fun clear() {
        deliveryDocuments.clear()
        productDocuments.clear()
        importABForms.clear()
        russianABForms.clear()
        productBatches.clear()
        setComponents.clear()
        transportConditions.clear()
        productVetDocuments.clear()
        mercuryNotActual.clear()
        complexDocuments.clear()
        invoiceInfo = null
    }
}