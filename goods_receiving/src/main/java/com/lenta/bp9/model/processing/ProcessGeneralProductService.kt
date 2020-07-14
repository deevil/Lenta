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

    fun countEqualOrigQuantityPPP (count: Double) : Boolean {
        return productInfo.origQuantity.toDouble() == ((taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProduct(productInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProduct(productInfo) ?: 0.0) + count)
    }

    fun countEqualOrigQuantityPGE (count: Double) : Boolean {
        return productInfo.origQuantity.toDouble() == ((taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProductPGE(productInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProductPGE(productInfo) ?: 0.0) + count)
    }

    fun countMoreOrigQuantityPPP (count: Double) : Boolean {
        return productInfo.origQuantity.toDouble() < ((taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProduct(productInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProduct(productInfo) ?: 0.0) + count)
    }

    fun categNormNotOrderMoreOrigQuantity () : Boolean {
        val countCategoryNorm = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.findLast {
            it.typeDiscrepancies == "1"
        }?.numberDiscrepancies?.toDouble() ?: 0.0
        val countCategoryNotOrder = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.findLast {
            it.typeDiscrepancies == "41"
        }?.numberDiscrepancies?.toDouble() ?: 0.0


        return productInfo.origQuantity.toDouble() < countCategoryNorm + countCategoryNotOrder
    }

    fun getOpenQuantityPGE(paramGrwOlGrundcat: String, paramGrwUlGrundcat: String) : Double {
        val countCategoryUnderload = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.findLast {
            it.typeDiscrepancies == paramGrwUlGrundcat
        }?.numberDiscrepancies?.toDouble() ?: 0.0
        val countCategorySurplus = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.findLast {
            it.typeDiscrepancies == paramGrwOlGrundcat
        }?.numberDiscrepancies?.toDouble() ?: 0.0
        val countAllCategory = (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProductPGE(productInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProductPGE(productInfo) ?: 0.0)
        return productInfo.orderQuantity.toDouble() - countCategoryUnderload - countAllCategory + countCategorySurplus
    }

    fun getRoundingQuantityPPP () : Double {
        return productInfo.origQuantity.toDouble() - ((taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProduct(productInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProduct(productInfo) ?: 0.0))
    }

    fun getRoundingQuantityPGE () : Double {
        return productInfo.orderQuantity.toDouble() - ((taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProductPGE(productInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProductPGE(productInfo) ?: 0.0))
    }

    fun checkParam (param: String) : Boolean {
        return !taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.filter {
            it.typeDiscrepancies == param
        }.isNullOrEmpty()
    }

    fun countWithoutParamGrsGrundNegPPP(paramGrsGrundNeg: String) : Double {
        return productInfo.origQuantity.toDouble() -
                (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProduct(productInfo) ?: 0.0) -
                (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProduct(productInfo) ?: 0.0) +
                (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountOfDiscrepanciesOfProduct(productInfo, paramGrsGrundNeg) ?: 0.0)
    }

    fun countWithoutParamGrwUlGrundcatPGE(paramGrwOlGrundcat: String, paramGrwUlGrundcat: String) : Double {
        return productInfo.orderQuantity.toDouble() - getOpenQuantityPGE(paramGrwOlGrundcat, paramGrwUlGrundcat)
    }

    fun removeDiscrepancyFromProduct(typeDiscrepancies: String) {
        taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.deleteProductDiscrepancy(materialNumber =  productInfo.materialNumber, typeDiscrepancies = typeDiscrepancies)
    }

    fun addWithoutUnderload(typeDiscrepancies: String, count: String, processingUnit: String) {
        val productDiscrepancy = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.findLast {
            it.typeDiscrepancies == typeDiscrepancies
        }
        if (productDiscrepancy != null) {
            taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.changeProductDiscrepancy(productDiscrepancy.copy(numberDiscrepancies = count))
        } else {
            add(count, typeDiscrepancies, processingUnit)
        }
    }

    fun countNormAndParamMoreOrderQuantityPGE (paramGrwOlGrundcat: String, count: Double) : Boolean {
        var countNormAndParam = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.filter {
            (it.typeDiscrepancies == "1" || it.typeDiscrepancies == paramGrwOlGrundcat) && it.processingUnitNumber == productInfo.processingUnit
        }?.sumByDouble {
            it.numberDiscrepancies.toDouble()
        } ?: 0.0
        countNormAndParam += count
        return countNormAndParam > productInfo.orderQuantity.toDouble()
    }

    fun addCountMoreCargoUnit(paramGrwOlGrundcat: String, count: Double, processingUnit: String) {
        var countNormAndParam = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.filter {
            it.typeDiscrepancies == "1" || it.typeDiscrepancies == paramGrwOlGrundcat
        }?.sumByDouble {
            it.numberDiscrepancies.toDouble()
        } ?: 0.0
        countNormAndParam += count
        taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.deleteProductDiscrepancy(materialNumber =  productInfo.materialNumber, typeDiscrepancies = "1")
        add(productInfo.orderQuantity, "1", processingUnit)
        taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.deleteProductDiscrepancy(materialNumber =  productInfo.materialNumber, typeDiscrepancies = paramGrwOlGrundcat)
        add((countNormAndParam - productInfo.orderQuantity.toDouble()).toString(), paramGrwOlGrundcat, processingUnit)
    }

    fun getQuantityAllCategoryPPP(count: Double) : Double {
        return ((taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProduct(productInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProduct(productInfo) ?: 0.0) + count)
    }

    fun getQuantityAllCategoryPGE(count: Double) : Double {
        return ((taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProductPGE(productInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProductPGE(productInfo) ?: 0.0) + count)
    }

    private fun getCountOfDiscrepancies(typeDiscrepancies: String) : Double {
        return taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountOfDiscrepanciesOfProduct(productInfo, typeDiscrepancies) ?: 0.0
    }

    fun overLimit(count: Double) : Boolean {
        return productInfo.origQuantity.toDouble() < ((taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProduct(productInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProduct(productInfo) ?: 0.0) + count)
    }

    fun add(count: String, typeDiscrepancies: String, processingUnit: String){
        val countAdd: Double = if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.RecalculationCargoUnit) {
            getCountOfDiscrepancies(typeDiscrepancies) + count.toDouble()
        } else {
            if (typeDiscrepancies == "1") count.toDouble() else getCountOfDiscrepancies(typeDiscrepancies) + count.toDouble()
        }
        val foundDiscrepancy = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.findLast {
            it.materialNumber == productInfo.materialNumber && it.typeDiscrepancies == typeDiscrepancies
        }

        if (countAdd == 0.0 && taskManager.getReceivingTask()?.taskHeader?.taskType != TaskType.ShipmentPP) {
            taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.deleteProductDiscrepancy(productInfo.materialNumber, typeDiscrepancies)
        } else {
            if (foundDiscrepancy == null) {
                taskManager.getReceivingTask()?.
                        taskRepository?.
                        getProductsDiscrepancies()?.
                        changeProductDiscrepancy(TaskProductDiscrepancies(
                                materialNumber = productInfo.materialNumber,
                                processingUnitNumber = processingUnit,
                                numberDiscrepancies = countAdd.toString(),
                                uom = productInfo.uom,
                                typeDiscrepancies = typeDiscrepancies,
                                isNotEdit = false,
                                isNew = productInfo.isGoodsAddedAsSurplus,
                                notEditNumberDiscrepancies = ""
                        ))
            } else {
                taskManager.getReceivingTask()?.
                        taskRepository?.
                        getProductsDiscrepancies()?.
                        changeProductDiscrepancy(foundDiscrepancy.copy(numberDiscrepancies = countAdd.toString(), processingUnitNumber = processingUnit))
            }
        }
    }

    fun addNotRecountPGE(countNorm: String, countDefect: String, typeDiscrepanciesDefect: String, processingUnit: String){
        val countAddDefect: Double = getCountOfDiscrepancies(typeDiscrepanciesDefect) + countDefect.toDouble()

        val foundNormDiscrepancy = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.findLast {
            it.materialNumber == productInfo.materialNumber && it.typeDiscrepancies == "1"
        }

        val foundDefectDiscrepancy = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.findLast {
            it.materialNumber == productInfo.materialNumber && it.typeDiscrepancies == typeDiscrepanciesDefect
        }

        foundNormDiscrepancy?.let {
            taskManager.getReceivingTask()?.
                    taskRepository?.
                    getProductsDiscrepancies()?.
                    changeProductDiscrepancyNotRecountPGE(it.copy(numberDiscrepancies = countNorm))
        }

        if (foundDefectDiscrepancy == null) {
            taskManager.getReceivingTask()?.
                    taskRepository?.
                    getProductsDiscrepancies()?.
                    changeProductDiscrepancy(TaskProductDiscrepancies(
                            materialNumber = productInfo.materialNumber,
                            processingUnitNumber = processingUnit,
                            numberDiscrepancies = countAddDefect.toString(),
                            uom = productInfo.uom,
                            typeDiscrepancies = typeDiscrepanciesDefect,
                            isNotEdit = false,
                            isNew = false,
                            notEditNumberDiscrepancies = ""
                    ))
        } else {
            taskManager.getReceivingTask()?.
                    taskRepository?.
                    getProductsDiscrepancies()?.
                    changeProductDiscrepancyNotRecountPGE(foundDefectDiscrepancy.copy(numberDiscrepancies = countAddDefect.toString(), processingUnitNumber = processingUnit))
        }
    }
}