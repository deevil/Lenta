package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductDiscrepancies
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.di.AppScope
import com.lenta.shared.models.core.ProductType
import javax.inject.Inject

@AppScope
class ProcessGeneralProductService
@Inject constructor() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var dataBase: IDataBaseRepo

    private lateinit var productInfo: TaskProductInfo

    fun newProcessGeneralProductService(productInfo: TaskProductInfo) : ProcessGeneralProductService? {
        return if (productInfo.type == ProductType.General){
            this.productInfo = productInfo.copy()
            this
        }
        else null
    }

    fun setProcessingUnitNumber(processingUnitNumber: String) {
        productInfo = productInfo.copy(processingUnit = processingUnitNumber)
        taskManager.getReceivingTask()?.taskRepository?.getProducts()?.changeProduct(productInfo)
    }

    fun countEqualOrigQuantity (count: Double) : Boolean {
        return productInfo.origQuantity.toDouble() == ((taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProduct(productInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProduct(productInfo) ?: 0.0) + count)
    }

    fun countLessEqualOrderQuantityPGE (count: Double) : Boolean {
        return productInfo.orderQuantity.toDouble() >= ((taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProductPGE(productInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProductPGE(productInfo) ?: 0.0) + count)
    }

    fun countLargerOrigQuantity (count: Double) : Boolean {
        return productInfo.origQuantity.toDouble() < ((taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProduct(productInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProduct(productInfo) ?: 0.0) + count)
    }

    fun categNormNotOrderLargerOrigQuantity () : Boolean {
        val countCategoryNorm = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.findLast {
            it.typeDiscrepancies == "1"
        }?.numberDiscrepancies?.toDouble() ?: 0.0
        val countCategoryNotOrder = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.findLast {
            it.typeDiscrepancies == "41"
        }?.numberDiscrepancies?.toDouble() ?: 0.0


        return productInfo.origQuantity.toDouble() < countCategoryNorm + countCategoryNotOrder
    }

    fun getRoundingQuantity (origQuantity: Double) : Double {
        return origQuantity - ((taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProduct(productInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProduct(productInfo) ?: 0.0))
    }

    fun paramGrsGrundNeg (paramGrsGrundNeg: String) : Boolean {
        return !taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.filter {
            it.typeDiscrepancies == paramGrsGrundNeg
        }.isNullOrEmpty()
    }

    fun countWithoutParamGrsGrundNeg(paramGrsGrundNeg: String) : Double {
        return productInfo.origQuantity.toDouble() -
                (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProduct(productInfo) ?: 0.0) -
                (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProduct(productInfo) ?: 0.0) +
                (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProductOfReasonRejection(productInfo, paramGrsGrundNeg) ?: 0.0)
    }

    fun delCategoryParamGrsGrundNeg(paramGrsGrundNeg: String) {
        val delProductDiscrepancy = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.findLast {
            it.typeDiscrepancies == paramGrsGrundNeg
        }
        if (delProductDiscrepancy != null) {
            taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.changeProductDiscrepancy(delProductDiscrepancy.copy(numberDiscrepancies = "0"))
        }
    }

    fun addWithoutUnderload(paramGrsGrundNeg: String, count: String) {
        val delProductDiscrepancy = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.findLast {
            it.typeDiscrepancies == paramGrsGrundNeg
        }
        if (delProductDiscrepancy != null) {
            taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.changeProductDiscrepancy(delProductDiscrepancy.copy(numberDiscrepancies = count))
        }
    }

    fun getQuantityCapitalized() : Double {
        return productInfo.quantityCapitalized.toDouble()
    }

    fun getQuantityAllCategory(count: Double) : Double {
        return ((taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProduct(productInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProduct(productInfo) ?: 0.0) + count)
    }

    private fun getCountRefusalOfReasonRejection(reasonRejectionCode: String) : Double {
        return taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountRefusalOfProductOfReasonRejection(productInfo, reasonRejectionCode)
    }

    fun add(count: String, reasonRejectionCode: String){
        val countAdd: Double = if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.RecalculationCargoUnit) {
            getCountRefusalOfReasonRejection(reasonRejectionCode) + count.toDouble()
        } else {
            if (reasonRejectionCode == "1") count.toDouble() else getCountRefusalOfReasonRejection(reasonRejectionCode) + count.toDouble()
        }
        val foundDiscrepancy = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.findLast {
            it.materialNumber == productInfo.materialNumber && it.typeDiscrepancies == reasonRejectionCode
        }

        if (foundDiscrepancy == null) {
            taskManager.getReceivingTask()?.
                    taskRepository?.
                    getProductsDiscrepancies()?.
                    changeProductDiscrepancy(TaskProductDiscrepancies(
                            materialNumber = productInfo.materialNumber,
                            exidv = "",
                            numberDiscrepancies = countAdd.toString(),
                            uom = productInfo.uom,
                            typeDiscrepancies = reasonRejectionCode,
                            isNotEdit = false,
                            isNew = false
                    ))
        } else {
            taskManager.getReceivingTask()?.
                    taskRepository?.
                    getProductsDiscrepancies()?.
                    changeProductDiscrepancy(foundDiscrepancy.copy(numberDiscrepancies = countAdd.toString()))
        }

        //Кол-во, которое было оприходовано по этому заказу и этому товару
        val quantityCapitalized = ((taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProduct(productInfo) ?: 0.0) +
                (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProduct(productInfo) ?: 0.0)).toString()

        productInfo = productInfo.copy(quantityCapitalized =  quantityCapitalized)

        taskManager.getReceivingTask()?.
                taskRepository?.
                getProducts()?.
                changeProduct(productInfo.copy(quantityCapitalized = quantityCapitalized))
    }
}