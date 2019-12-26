package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskMercuryDiscrepancies
import com.lenta.bp9.model.task.TaskProductDiscrepancies
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.revise.ProductVetDocumentRevise
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.di.AppScope
import com.lenta.shared.utilities.Logg
import javax.inject.Inject

const val PROCESSING_MERCURY_SAVED = 1
const val PROCESSING_MERCURY_QUANT_GREAT_IN_VET_DOC = 2
const val PROCESSING_MERCURY_QUANT_GREAT_IN_INVOICE = 3

@AppScope
class ProcessMercuryProductService
@Inject constructor() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    private lateinit var productInfo: TaskProductInfo
    private val newProductDiscrepancies: ArrayList<TaskProductDiscrepancies> = ArrayList()
    private val newVetProductDiscrepancies: ArrayList<TaskMercuryDiscrepancies> = ArrayList()
    private var isModifications: Boolean = false

    fun newProcessMercuryProductService(productInfo: TaskProductInfo) : ProcessMercuryProductService? {
        return if (productInfo.isVet){
            this.productInfo = productInfo.copy()
            newProductDiscrepancies.clear()
            taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.map {
                newProductDiscrepancies.add(it.copy())
            }
            newVetProductDiscrepancies.clear()
            taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryDiscrepanciesOfProduct(productInfo)?.map {
                newVetProductDiscrepancies.add(it)
            }
            isModifications = false
            this
        }
        else null
    }

    fun add(countForProduct: String, countForVetDoc: String, reasonRejectionCode: String, manufacturer: String, productionDate: String){
        isModifications = true
        val countAddForProduct = if (reasonRejectionCode == "1") countForProduct.toDouble() else getNewCountRefusalOfReasonRejection(reasonRejectionCode) + countForProduct.toDouble()
        var foundDiscrepancy = newProductDiscrepancies.findLast {
            it.typeDiscrepancies == reasonRejectionCode
        }

        foundDiscrepancy = foundDiscrepancy?.copy(numberDiscrepancies = countAddForProduct.toString())
                ?: TaskProductDiscrepancies(
                        materialNumber = productInfo.materialNumber,
                        exidv = "",
                        numberDiscrepancies = countAddForProduct.toString(),
                        uom = productInfo.uom,
                        typeDiscrepancies = reasonRejectionCode,
                        isNotEdit = false,
                        isNew = false
                )

        val vetDocumentVolume = taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryInfoOfProduct(productInfo)?.filter {
            it.manufacturer == manufacturer &&
                    it.productionDate == productionDate
        }?.sumByDouble {
            it.volume
        } ?: 0.0
        val vetDocumentCountAlreadyAdded = newVetProductDiscrepancies.filter {
            it.manufacturer == manufacturer &&
                    it.productionDate == productionDate &&
                    it.typeDiscrepancies == reasonRejectionCode
        }.sumByDouble {
            it.numberDiscrepancies
        }
        var countAllAdd = countForVetDoc.toDouble() + vetDocumentCountAlreadyAdded

        val foundVetDiscrepancy = taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryInfoOfProduct(productInfo)?.filter {
            it.manufacturer == manufacturer &&
                    it.productionDate == productionDate
        }?.map {
            val addCount: Double
            if (vetDocumentVolume <= countAllAdd && countAllAdd > 0.0) {
                addCount = vetDocumentVolume
                countAllAdd -= vetDocumentVolume
            } else {
                addCount = countAllAdd
                countAllAdd = 0.0
            }

            TaskMercuryDiscrepancies(
                    materialNumber = it.materialNumber,
                    vetDocumentID = it.vetDocumentID,
                    volume = it.volume,
                    uom = productInfo.uom,
                    typeDiscrepancies = reasonRejectionCode,
                    numberDiscrepancies = addCount,
                    productionDate = it.productionDate,
                    manufacturer = it.manufacturer,
                    productionDateTo = it.productionDateTo
            )
        }

        changeNewProductDiscrepancy(foundDiscrepancy)
        foundVetDiscrepancy?.map {
            changeNewVetProductDiscrepancy(it)
        }
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

    private fun changeNewProductDiscrepancy(newDiscrepancy: TaskProductDiscrepancies) {
        var index = -1
        for (i in newProductDiscrepancies.indices) {
            if (newDiscrepancy.typeDiscrepancies == newProductDiscrepancies[i].typeDiscrepancies) {
                index = i
            }
        }

        if (index != -1) {
            newProductDiscrepancies.removeAt(index)
        }

        newProductDiscrepancies.add(newDiscrepancy)
    }

    private fun changeNewVetProductDiscrepancy(newVetDiscrepancy: TaskMercuryDiscrepancies) {
        var index = -1
        for (i in newVetProductDiscrepancies.indices) {
            if (newVetDiscrepancy.manufacturer == newVetProductDiscrepancies[i].manufacturer &&
                    newVetDiscrepancy.productionDate == newVetProductDiscrepancies[i].productionDate &&
                    newVetDiscrepancy.vetDocumentID == newVetProductDiscrepancies[i].vetDocumentID &&
                    newVetDiscrepancy.typeDiscrepancies == newVetProductDiscrepancies[i].typeDiscrepancies) {
                index = i
            }
        }

        if (index != -1) {
            newVetProductDiscrepancies.removeAt(index)
        }

        newVetProductDiscrepancies.add(newVetDiscrepancy)
    }

    fun checkConditionsOfPreservation(count: String, reasonRejectionCode: String, manufacturer: String, productionDate: String) : Int {

        val vetDocumentIDVolume = taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryInfoOfProduct(productInfo)?.filter {
            it.manufacturer == manufacturer &&
                    it.productionDate == productionDate
        }?.sumByDouble {
            it.volume
        } ?: 0.0

        if ( getQuantityAllCategoryExceptNonOrderOfVetDoc(if (reasonRejectionCode != "41") count.toDouble() else 0.0, manufacturer, productionDate) > vetDocumentIDVolume ) {
            return PROCESSING_MERCURY_QUANT_GREAT_IN_VET_DOC
        }

        if ( getQuantityAllCategoryExceptNonOrderOfVetDoc(if (reasonRejectionCode != "41") count.toDouble() else 0.0, manufacturer, productionDate) <= productInfo.orderQuantity.toDouble() ) {
            return PROCESSING_MERCURY_SAVED
        } else {
            if (productInfo.uom.name == "ШТ") {
                return PROCESSING_MERCURY_QUANT_GREAT_IN_INVOICE
            }
        }

        return if (getQuantityAllCategoryExceptNonOrderOfProduct(if (reasonRejectionCode != "41") count.toDouble() else 0.0) <= (productInfo.origQuantity.toDouble() + productInfo.overdToleranceLimit.toDouble())) {
            PROCESSING_MERCURY_SAVED
        } else {
            PROCESSING_MERCURY_QUANT_GREAT_IN_INVOICE
        }
    }

    private fun getQuantityAllCategoryExceptNonOrderOfVetDoc(count: Double, manufacturer: String, productionDate: String) : Double {
        return (newVetProductDiscrepancies.filter {newMercuryDiscrepancies ->
            newMercuryDiscrepancies.typeDiscrepancies != "41" && newMercuryDiscrepancies.manufacturer == manufacturer && newMercuryDiscrepancies.productionDate == productionDate
        }.sumByDouble {
            it.numberDiscrepancies
        }) + count
    }

    private fun getQuantityAllCategoryExceptNonOrderOfProduct(count: Double) : Double {
        return (newProductDiscrepancies.filter {newDiscrepancies ->
            newDiscrepancies.typeDiscrepancies != "41"
        }.sumByDouble {
            it.numberDiscrepancies.toDouble()
        } ) + count
    }

    fun getGoodsDetails() : List<TaskProductDiscrepancies>? {
        return newProductDiscrepancies
    }

    fun getNewCountAccept() : Double {
        return newProductDiscrepancies.filter {
            it.typeDiscrepancies == "1"
        }.sumByDouble {
            it.numberDiscrepancies.toDouble()
        }
    }

    fun getNewCountRefusal() : Double {
        return newProductDiscrepancies.filter {
            it.typeDiscrepancies != "1"
        }.sumByDouble {
            it.numberDiscrepancies.toDouble()
        }
    }

    fun getNewCountRefusalOfReasonRejection(reasonRejectionCode: String) : Double {
        var countRefusal = 0.0
        newProductDiscrepancies.filter {
            it.typeDiscrepancies == reasonRejectionCode
        }.map {discrepancies ->
            countRefusal += discrepancies.numberDiscrepancies.toDouble()
        }
        return countRefusal
    }

    fun modifications() : Boolean {
        return isModifications
    }

}