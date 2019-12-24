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
const val PROCESSING_MERCURY_QUANT_GREAT_IN_INVOICE = 2
const val PROCESSING_MERCURY_QUANT_GREAT_IN_ORDER = 3

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

    fun newProcessMercuryProductService(productInfo: TaskProductInfo) : ProcessMercuryProductService? {
        return if (productInfo.isVet){
            this.productInfo = productInfo.copy()
            newProductDiscrepancies.clear()
            taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.map {
                newProductDiscrepancies.add(it.copy())
            }
            newVetProductDiscrepancies.clear()
            this
        }
        else null
    }

    fun add(count: String, reasonRejectionCode: String, manufacturer: String, productionDate: String){
        var foundDiscrepancy = newProductDiscrepancies.findLast {
            it.typeDiscrepancies == reasonRejectionCode
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

        changeNewProductDiscrepancy(foundDiscrepancy)

        var countAll = count.toDouble()
        val vetDocumentIDVolume = taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryInfoOfProduct(productInfo)?.filter {
            it.manufacturer == manufacturer &&
                    it.productionDate == productionDate
        }?.sumByDouble {
            it.volume
        } ?: 0.0

        val foundVetDiscrepancy = taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryInfoOfProduct(productInfo)?.filter {
            it.manufacturer == manufacturer &&
                    it.productionDate == productionDate
        }?.map {
            val addCount: Double
            if (vetDocumentIDVolume <= countAll && countAll > 0.0) {
                addCount = vetDocumentIDVolume
                countAll -= vetDocumentIDVolume
            } else {
                addCount = countAll
                countAll = 0.0
            }

            Logg.d { "testddi addCount $addCount" }
            Logg.d { "testddi countAll $countAll" }
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

        /**var foundVetDiscrepancy = newVetProductDiscrepancies.filter {
            it.manufacturer == manufacturer &&
                    it.productionDate == productionDate &&
                    it.typeDiscrepancies == reasonRejectionCode
        }.map {
            val addCount: Double
            if (vetDocumentIDVolume <= countAll && countAll > 0.0) {
                addCount = vetDocumentIDVolume
                countAll -= vetDocumentIDVolume
            } else {
                addCount = countAll
                countAll = 0.0
            }

            TaskMercuryDiscrepancies(
                    materialNumber = it.materialNumber,
                    vetDocumentID = it.vetDocumentID,
                    volume = it.volume,
                    uom = productInfo.uom,
                    typeDiscrepancies = it.typeDiscrepancies,
                    numberDiscrepancies = addCount,
                    productionDate = it.productionDate,
                    manufacturer = it.manufacturer,
                    productionDateTo = it.productionDateTo
            )
        }*/

        foundVetDiscrepancy?.map {
            changeNewVetProductDiscrepancy(it)
        }
        Logg.d { "testddi foundVetDiscrepancy $foundVetDiscrepancy" }
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
            if (newDiscrepancy.materialNumber == newProductDiscrepancies[i].materialNumber && newDiscrepancy.typeDiscrepancies == newProductDiscrepancies[i].typeDiscrepancies) {
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
            if (newVetDiscrepancy.materialNumber == newVetProductDiscrepancies[i].materialNumber &&
                    newVetDiscrepancy.manufacturer == newVetProductDiscrepancies[i].manufacturer &&
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

        Logg.d { "testddi getQuantityAllCategoryExceptNonOrderOfVetDoc ${getQuantityAllCategoryExceptNonOrderOfVetDoc(if (reasonRejectionCode != "41") count.toDouble() else 0.0, manufacturer, productionDate)}" }
        Logg.d { "testddi vetDocumentIDVolume $vetDocumentIDVolume" }
        Logg.d { "testddi getQuantityAllCategoryExceptNonOrderOfProduct ${getQuantityAllCategoryExceptNonOrderOfProduct(if (reasonRejectionCode != "41") count.toDouble() else 0.0)}" }
        Logg.d { "testddi count $count" }
        if ( getQuantityAllCategoryExceptNonOrderOfVetDoc(if (reasonRejectionCode != "41") count.toDouble() else 0.0, manufacturer, productionDate) <= vetDocumentIDVolume ) {
            return PROCESSING_MERCURY_SAVED
        }

        if (productInfo.uom.name == "ШТ") {
            return PROCESSING_MERCURY_QUANT_GREAT_IN_INVOICE
        }

        return if (getQuantityAllCategoryExceptNonOrderOfProduct(if (reasonRejectionCode != "41") count.toDouble() else 0.0) <= (productInfo.origQuantity.toDouble() + productInfo.overdToleranceLimit.toDouble())) {
            PROCESSING_MERCURY_SAVED
        } else {
            PROCESSING_MERCURY_QUANT_GREAT_IN_ORDER
        }
    }

    private fun getQuantityAllCategoryExceptNonOrderOfVetDoc(count: Double, manufacturer: String, productionDate: String) : Double {
        /**return (taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryDiscrepanciesOfProduct(productInfo)?.filter {mercuryDiscrepancies ->
            mercuryDiscrepancies.typeDiscrepancies != "41" && mercuryDiscrepancies.manufacturer == manufacturer && mercuryDiscrepancies.productionDate == productionDate
        }?.plus(newVetProductDiscrepancies.filter {newMercuryDiscrepancies ->
            newMercuryDiscrepancies.typeDiscrepancies != "41" && newMercuryDiscrepancies.manufacturer == manufacturer && newMercuryDiscrepancies.productionDate == productionDate
        })?.sumByDouble {
            it.numberDiscrepancies
        } ?: 0.0) + count*/
        newVetProductDiscrepancies.filter {newMercuryDiscrepancies ->
            newMercuryDiscrepancies.typeDiscrepancies != "41" && newMercuryDiscrepancies.manufacturer == manufacturer && newMercuryDiscrepancies.productionDate == productionDate
        }.map {
            Logg.d { "testddi $it"}
        }
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
        return taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.plus(newProductDiscrepancies)
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

    fun getNewCountRefusalOfReasonRejection(reasonRejectionCode: String?) : Double {
        var countRefusal = 0.0
        reasonRejectionCode?.let {
            newProductDiscrepancies.filter {
                it.typeDiscrepancies == reasonRejectionCode
            }.map {discrepancies ->
                countRefusal += discrepancies.numberDiscrepancies.toDouble()
            }
        }
        return countRefusal
    }

    fun getCountProductNotProcessed() : Double {
        return (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountProductNotProcessedOfProduct(productInfo) ?: 0.0) - getNewCountAccept() - getNewCountRefusal()
    }

}