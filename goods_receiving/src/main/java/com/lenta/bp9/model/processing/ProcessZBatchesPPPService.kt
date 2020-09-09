package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductDiscrepancies
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.TaskZBatchesDiscrepancies
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
import com.lenta.shared.di.AppScope
import javax.inject.Inject

@AppScope
class ProcessZBatchesPPPService
@Inject constructor() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    private lateinit var productInfo: TaskProductInfo
    val taskRepository by lazy { taskManager.getReceivingTask()?.taskRepository }

    fun newProcessZBatchesPPPService(initProductInfo: TaskProductInfo): ProcessZBatchesPPPService? {
        return if (initProductInfo.isZBatches && !initProductInfo.isVet) { //см. SearchProductDelegate
            this.productInfo = initProductInfo.copy()
            this
        } else null
    }

    private fun getCountAcceptOfProduct(): Double {
        return taskRepository
                ?.getProductsDiscrepancies()
                ?.getCountAcceptOfProduct(productInfo)
                ?: 0.0
    }

    private fun getCountRefusalOfProduct(): Double {
        return taskRepository
                ?.getProductsDiscrepancies()
                ?.getCountRefusalOfProduct(productInfo)
                ?: 0.0
    }

    private fun getCountOfDiscrepancies(typeDiscrepancies: String): Double {
        return taskRepository
                ?.getProductsDiscrepancies()
                ?.getCountOfDiscrepanciesOfProduct(productInfo, typeDiscrepancies)
                ?: 0.0
    }

    private fun getTotalCountDiscrepancies(count: Double): Double {
        return getCountAcceptOfProduct() + getCountRefusalOfProduct() + count
    }

    fun countEqualOrigQuantity(count: Double): Boolean {
        return productInfo.origQuantity.toDouble() == getTotalCountDiscrepancies(count)
    }

    fun countMoreOrigQuantity(count: Double): Boolean {
        return productInfo.origQuantity.toDouble() < getTotalCountDiscrepancies(count)
    }

    fun categNormNotOrderMoreOrigQuantity(): Boolean {
        val countCategoryNorm =
                taskRepository
                        ?.getProductsDiscrepancies()
                        ?.findProductDiscrepanciesOfProduct(productInfo)
                        ?.findLast { it.typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM }
                        ?.numberDiscrepancies?.toDouble()
                        ?: 0.0

        val countCategoryNotOrder =
                taskRepository
                        ?.getProductsDiscrepancies()
                        ?.findProductDiscrepanciesOfProduct(productInfo)
                        ?.findLast { it.typeDiscrepancies == "41" }
                        ?.numberDiscrepancies
                        ?.toDouble()
                        ?: 0.0

        return productInfo.origQuantity.toDouble() < (countCategoryNorm + countCategoryNotOrder)
    }

    fun getRoundingQuantity(): Double {
        return productInfo.origQuantity.toDouble() - (getCountAcceptOfProduct() + getCountRefusalOfProduct())
    }

    fun checkParam(param: String): Boolean {
        return !taskRepository
                ?.getProductsDiscrepancies()
                ?.findProductDiscrepanciesOfProduct(productInfo)
                ?.filter { it.typeDiscrepancies == param }
                .isNullOrEmpty()
    }

    fun countWithoutParamGrsGrundNeg(paramGrsGrundNeg: String): Double {
        return productInfo.origQuantity.toDouble() - getCountAcceptOfProduct() - getCountRefusalOfProduct() + getCountOfDiscrepancies(paramGrsGrundNeg)
    }

    fun removeDiscrepancyFromProduct(typeDiscrepancies: String) {
        taskRepository
                ?.getProductsDiscrepancies()
                ?.deleteProductDiscrepancy(
                        materialNumber = productInfo.materialNumber,
                        typeDiscrepancies = typeDiscrepancies
                )
    }

    fun addWithoutUnderload(typeDiscrepancies: String, count: String, manufactureCode: String, shelfLifeDate: String, shelfLifeTime: String) {
        val productDiscrepancy =
                taskRepository
                        ?.getProductsDiscrepancies()
                        ?.findProductDiscrepanciesOfProduct(productInfo)
                        ?.findLast { it.typeDiscrepancies == typeDiscrepancies }

        if (productDiscrepancy != null) {
            taskRepository
                    ?.getProductsDiscrepancies()
                    ?.changeProductDiscrepancy(productDiscrepancy.copy(numberDiscrepancies = count))
        } else {
            add(count, typeDiscrepancies, manufactureCode, shelfLifeDate, shelfLifeTime)
        }
    }

    fun getQuantityAllCategory(count: Double): Double {
        return getCountAcceptOfProduct() + getCountRefusalOfProduct() + count
    }

    fun overLimit(count: Double): Boolean {
        val totalCount = getCountAcceptOfProduct() + getCountRefusalOfProduct() + count

        return productInfo.origQuantity.toDouble() < totalCount
    }

    fun add(count: String, typeDiscrepancies: String, manufactureCode: String, shelfLifeDate: String, shelfLifeTime: String) {
        val countAdd =
                if (typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM) {
                    count.toDouble()
                } else {
                    getCountOfDiscrepancies(typeDiscrepancies) + count.toDouble()
                }

        changeProductDiscrepancy(countAdd.toString(), typeDiscrepancies)

        if (typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM) {
            changeZBatchDiscrepancy(countAdd.toString(), typeDiscrepancies, manufactureCode, shelfLifeDate, shelfLifeTime)
        }
    }

    private fun changeProductDiscrepancy(countAdd: String, typeDiscrepancies: String) {
        var foundDiscrepancy =
                taskRepository
                        ?.getProductsDiscrepancies()
                        ?.findProductDiscrepanciesOfProduct(productInfo)
                        ?.findLast { it.typeDiscrepancies == typeDiscrepancies }

        foundDiscrepancy =
                foundDiscrepancy
                        ?.copy(numberDiscrepancies = countAdd)
                        ?: TaskProductDiscrepancies(
                                materialNumber = productInfo.materialNumber,
                                processingUnitNumber = productInfo.processingUnit,
                                numberDiscrepancies = countAdd,
                                uom = productInfo.uom,
                                typeDiscrepancies = typeDiscrepancies,
                                isNotEdit = false,
                                isNew = false,
                                notEditNumberDiscrepancies = ""
                        )

        taskRepository
                ?.getProductsDiscrepancies()
                ?.changeProductDiscrepancy(foundDiscrepancy)
    }

    private fun changeZBatchDiscrepancy(countAdd: String, typeDiscrepancies: String, manufactureCode: String, shelfLifeDate: String, shelfLifeTime: String) {
        var foundDiscrepancy =
                taskRepository
                        ?.getZBatchesDiscrepancies()
                        ?.findZBatchDiscrepanciesOfProduct(productInfo.materialNumber)
                        ?.findLast { it.typeDiscrepancies == typeDiscrepancies }

        foundDiscrepancy =
                foundDiscrepancy
                        ?.copy(numberDiscrepancies = countAdd)
                        ?: TaskZBatchesDiscrepancies(
                                processingUnit = "",
                                materialNumber = productInfo.materialNumber,
                                batchNumber = "",
                                numberDiscrepancies = countAdd,
                                uom = productInfo.uom,
                                typeDiscrepancies = typeDiscrepancies,
                                isNew = false,
                                manufactureCode = manufactureCode,
                                shelfLifeDate = shelfLifeDate,
                                shelfLifeTime = shelfLifeTime
                        )

        taskRepository
                ?.getZBatchesDiscrepancies()
                ?.changeZBatchDiscrepancy(foundDiscrepancy)
    }

}