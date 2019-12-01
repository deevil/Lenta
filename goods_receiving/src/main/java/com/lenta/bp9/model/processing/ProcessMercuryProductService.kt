package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskMercuryDiscrepancies
import com.lenta.bp9.model.task.TaskProductDiscrepancies
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.revise.ProductVetDocumentRevise
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.di.AppScope
import javax.inject.Inject

const val PROCESSING_MERCURY_SAVED = 1
const val PROCESSING_MERCURY_QUANT_GREAT_IN_INVOICE = 2
const val PROCESSING_MERCURY_QUANT_GREAT_IN_ORDER = 3
const val PROCESSING_MERCURY_QUANT_LESS_THAN_IN_VAD = 4
const val PROCESSING_MERCURY_QUANT_MORE_THAN_IN_VAD =5

@AppScope
class ProcessMercuryProductService
@Inject constructor() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    private lateinit var productInfo: TaskProductInfo
    private var vetDocument: ProductVetDocumentRevise? = null
    private val newProductDiscrepancies: ArrayList<TaskProductDiscrepancies> = ArrayList()
    private val newVetProductDiscrepancies: ArrayList<TaskMercuryDiscrepancies> = ArrayList()

    fun newProcessMercuryProductService(productInfo: TaskProductInfo) : ProcessMercuryProductService? {
        return if (productInfo.isVet){
            this.productInfo = productInfo.copy()
            vetDocument = taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getProductVetDocuments()?.findLast {
                it.productNumber == productInfo.materialNumber
            }
            taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.map {
                newProductDiscrepancies.add(it)
            }
            taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryDiscrepanciesOfProduct(productInfo)?.map {
                newVetProductDiscrepancies.add(it)
            }
            this
        }
        else null
    }

    fun add(count: String, reasonRejectionCode: String, manufacturer: String, productionDate: String){
        var foundDiscrepancy = newProductDiscrepancies.findLast {
            it.materialNumber == productInfo.materialNumber && it.typeDiscrepancies == reasonRejectionCode
        }

        foundDiscrepancy = foundDiscrepancy?.copy(numberDiscrepancies = count)
                ?: TaskProductDiscrepancies(
                        materialNumber = productInfo.materialNumber,
                        exidv = "",
                        numberDiscrepancies = count,
                        uom = productInfo.uom,
                        typeDiscrepancies = reasonRejectionCode,
                        isNotEdit = false,
                        isNew = false
                )

        var foundVetDiscrepancy = newVetProductDiscrepancies.findLast {
            it.materialNumber == productInfo.materialNumber && it.typeDiscrepancies == reasonRejectionCode
        }

        foundVetDiscrepancy = foundVetDiscrepancy?.copy(numberDiscrepancies = count.toDouble())
                ?: TaskMercuryDiscrepancies(
                        materialNumber = productInfo.materialNumber,
                        vetDocumentID = vetDocument?.vetDocumentID ?: "",
                        volume = vetDocument?.volume ?: 0.0,
                        uom = productInfo.uom,
                        typeDiscrepancies = reasonRejectionCode,
                        numberDiscrepancies = count.toDouble(),
                        productionDate = productionDate,
                        manufacturer = manufacturer,
                        productionDateTo = ""
                )

        changeNewDiscrepancy(foundDiscrepancy, foundVetDiscrepancy)
    }

    fun save(){
        if (newProductDiscrepancies.isNotEmpty()) {
            newProductDiscrepancies.map {
                taskManager.getReceivingTask()?.
                        taskRepository?.
                        getProductsDiscrepancies()?.
                        changeProductDiscrepancy(it)
            }
        }

        //Кол-во, которое было оприходовано по этому заказу и этому товару
        val quantityCapitalized = ((taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProduct(productInfo) ?: 0.0) +
                (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProduct(productInfo) ?: 0.0)).toString()

        productInfo = productInfo.copy(quantityCapitalized =  quantityCapitalized)

        taskManager.getReceivingTask()?.
                taskRepository?.
                getProducts()?.
                changeProduct(productInfo.copy(quantityCapitalized = quantityCapitalized))


        if (newVetProductDiscrepancies.isNotEmpty()) {
            newVetProductDiscrepancies.map {
                taskManager.getReceivingTask()?.
                        taskRepository?.
                        getMercuryDiscrepancies()?.
                        changeMercuryDiscrepancy(it)
            }
        }
    }

    private fun changeNewDiscrepancy(newDiscrepancy: TaskProductDiscrepancies, newVetDiscrepancy: TaskMercuryDiscrepancies) {
        deleteProductDiscrepancy(newDiscrepancy)
        addProductDiscrepancy(newDiscrepancy)
        deleteVetProductDiscrepancy(newVetDiscrepancy)
        addVetProductDiscrepancy(newVetDiscrepancy)
    }

    private fun deleteProductDiscrepancy(discrepancy: TaskProductDiscrepancies) : Boolean {
        var index = -1
        for (i in newProductDiscrepancies.indices) {
            if (discrepancy.materialNumber == newProductDiscrepancies[i].materialNumber && discrepancy.typeDiscrepancies == newProductDiscrepancies[i].typeDiscrepancies) {
                index = i
            }
        }

        if (index == -1) {
            return false
        }
        newProductDiscrepancies.removeAt(index)
        return true
    }

    private fun addProductDiscrepancy(discrepancy: TaskProductDiscrepancies): Boolean {
        var index = -1
        for (i in newProductDiscrepancies.indices) {
            if (discrepancy.materialNumber == newProductDiscrepancies[i].materialNumber && discrepancy.typeDiscrepancies == newProductDiscrepancies[i].typeDiscrepancies) {
                index = i
            }
        }

        if (index == -1) {
            newProductDiscrepancies.add(discrepancy)
            return true
        }
        return false
    }

    private fun deleteVetProductDiscrepancy(discrepancy: TaskMercuryDiscrepancies) : Boolean {
        var index = -1
        for (i in newVetProductDiscrepancies.indices) {
            if (discrepancy.materialNumber == newVetProductDiscrepancies[i].materialNumber && discrepancy.typeDiscrepancies == newVetProductDiscrepancies[i].typeDiscrepancies) {
                index = i
            }
        }

        if (index == -1) {
            return false
        }
        newVetProductDiscrepancies.removeAt(index)
        return true
    }

    private fun addVetProductDiscrepancy(discrepancy: TaskMercuryDiscrepancies): Boolean {
        var index = -1
        for (i in newVetProductDiscrepancies.indices) {
            if (discrepancy.materialNumber == newVetProductDiscrepancies[i].materialNumber && discrepancy.typeDiscrepancies == newVetProductDiscrepancies[i].typeDiscrepancies) {
                index = i
            }
        }

        if (index == -1) {
            newVetProductDiscrepancies.add(discrepancy)
            return true
        }
        return false
    }

    fun checkConditionsOfPreservation(count: String, reasonRejectionCode: String) : Int {
        val quantityInOrder = repoInMemoryHolder.invoiceContents.value?.findLast {
            it.materialNumber == productInfo.materialNumber
        }?.quantityInOrder ?: 0.0

        if ( getQuantityAllCategoryExceptNonOrder(if (reasonRejectionCode != "41") count.toDouble() else 0.0) <= quantityInOrder ) {
            return PROCESSING_MERCURY_SAVED
        }

        if (productInfo.uom.name == "шт") {
            return PROCESSING_MERCURY_QUANT_GREAT_IN_INVOICE
        }

        if (getQuantityAllCategoryExceptNonOrder(if (reasonRejectionCode != "41") count.toDouble() else 0.0) > (productInfo.origQuantity.toDouble() + productInfo.overdToleranceLimit.toDouble()) ) {
            return PROCESSING_MERCURY_QUANT_GREAT_IN_ORDER
        }

        return if (getQuantityAllCategoryExceptNonOrder(if (reasonRejectionCode != "41") count.toDouble() else 0.0) <= (vetDocument?.volume ?: 0.0)) {
            PROCESSING_MERCURY_QUANT_LESS_THAN_IN_VAD
        } else {
            PROCESSING_MERCURY_QUANT_MORE_THAN_IN_VAD
        }
    }

    private fun getQuantityAllCategoryExceptNonOrder(count: Double) : Double {
        return taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryDiscrepanciesOfProduct(productInfo)?.filter {
            it.typeDiscrepancies != "41"
        }?.sumByDouble {
            it.numberDiscrepancies
        } ?: 0.0 + count
    }

}