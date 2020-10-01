package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.*
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

    private fun getCountOfDiscrepanciesOfProduct(typeDiscrepancies: String, processingUnitNumber: String): Double {
        return taskRepository
                ?.getProductsDiscrepancies()
                ?.getCountOfDiscrepanciesOfProductOfProcessingUnit(productInfo, typeDiscrepancies, processingUnitNumber)
                ?: 0.0
    }

    private fun getCountOfDiscrepanciesOfProductOfZBatch(typeDiscrepancies: String, manufactureCode: String, shelfLifeDate: String, shelfLifeTime: String): Double {
        return taskRepository
                ?.getZBatchesDiscrepancies()
                ?.findZBatchDiscrepanciesOfProduct(productInfo.materialNumber)
                ?.findLast {
                    it.typeDiscrepancies == typeDiscrepancies
                            && it.manufactureCode == manufactureCode
                            && it.shelfLifeDate == shelfLifeDate
                            && it.shelfLifeTime == shelfLifeTime
                }
                ?.numberDiscrepancies
                ?.toDoubleOrNull()
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
        return productInfo.origQuantity.toDouble() - getCountAcceptOfProduct() - getCountRefusalOfProduct() + getCountOfDiscrepanciesOfProduct(paramGrsGrundNeg, productInfo.processingUnit)
    }

    fun removeDiscrepancyFromProduct(typeDiscrepancies: String) {
        taskRepository
                ?.getProductsDiscrepancies()
                ?.deleteProductDiscrepancy(
                        materialNumber = productInfo.materialNumber,
                        typeDiscrepancies = typeDiscrepancies
                )
    }

    fun addWithoutUnderload(typeDiscrepancies: String, count: String, manufactureCode: String, shelfLifeDate: String, shelfLifeTime: String, productionDate: String, partySignsType: PartySignsTypeOfZBatches) {
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
            add(count, typeDiscrepancies, manufactureCode, shelfLifeDate, shelfLifeTime, productionDate, partySignsType)
        }
    }

    fun getQuantityAllCategory(): Double {
        return getCountAcceptOfProduct() + getCountRefusalOfProduct()
    }

    fun overLimit(count: Double): Boolean {
        val totalCount = getCountAcceptOfProduct() + getCountRefusalOfProduct() + count

        return productInfo.origQuantity.toDouble() < totalCount
    }

    fun add(count: String, typeDiscrepancies: String, manufactureCode: String, shelfLifeDate: String, shelfLifeTime: String, productionDate: String, partySignsType: PartySignsTypeOfZBatches) {
        changeProductDiscrepancy(count, typeDiscrepancies)

        if (typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM) {
            changeZBatchDiscrepancy(count, typeDiscrepancies, manufactureCode, shelfLifeDate, shelfLifeTime)
            changePartySign(typeDiscrepancies, manufactureCode, shelfLifeDate, shelfLifeTime, productionDate, partySignsType)
        }
    }

    private fun changeProductDiscrepancy(count: String, typeDiscrepancies: String) {
        val countAdd = getCountOfDiscrepanciesOfProduct(typeDiscrepancies, productInfo.processingUnit) + count.toDouble()

        var foundDiscrepancy =
                taskRepository
                        ?.getProductsDiscrepancies()
                        ?.findProductDiscrepanciesOfProduct(productInfo)
                        ?.findLast { it.typeDiscrepancies == typeDiscrepancies }

        foundDiscrepancy =
                foundDiscrepancy
                        ?.copy(numberDiscrepancies = countAdd.toString())
                        ?: TaskProductDiscrepancies(
                                materialNumber = productInfo.materialNumber,
                                processingUnitNumber = productInfo.processingUnit,
                                numberDiscrepancies = countAdd.toString(),
                                uom = productInfo.purchaseOrderUnits,
                                typeDiscrepancies = typeDiscrepancies,
                                isNotEdit = false,
                                isNew = false,
                                notEditNumberDiscrepancies = ""
                        )

        taskRepository
                ?.getProductsDiscrepancies()
                ?.changeProductDiscrepancy(foundDiscrepancy)
    }

    private fun changeZBatchDiscrepancy(count: String, typeDiscrepancies: String, manufactureCode: String, shelfLifeDate: String, shelfLifeTime: String) {
        val countAdd = getCountOfDiscrepanciesOfProductOfZBatch(typeDiscrepancies, manufactureCode, shelfLifeDate, shelfLifeTime) + count.toDouble()

        var foundDiscrepancy =
                taskRepository
                        ?.getZBatchesDiscrepancies()
                        ?.findZBatchDiscrepanciesOfProduct(productInfo.materialNumber)
                        ?.findLast {
                            it.typeDiscrepancies == typeDiscrepancies
                                    && it.manufactureCode == manufactureCode
                                    && it.shelfLifeDate == shelfLifeDate
                                    && it.shelfLifeTime == shelfLifeTime
                        }

        foundDiscrepancy =
                foundDiscrepancy
                        ?.copy(numberDiscrepancies = countAdd.toString())
                        ?: TaskZBatchesDiscrepancies(
                                processingUnit = productInfo.processingUnit,
                                materialNumber = productInfo.materialNumber,
                                batchNumber = "",
                                numberDiscrepancies = countAdd.toString(),
                                uom = productInfo.purchaseOrderUnits,
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

    private fun changePartySign(typeDiscrepancies: String, manufactureCode: String, shelfLifeDate: String, shelfLifeTime: String, productionDate: String, partySignsType: PartySignsTypeOfZBatches) {

        var foundDiscrepancy =
                taskRepository
                        ?.getZBatchesDiscrepancies()
                        ?.findPartySignsOfProduct(productInfo.materialNumber, productInfo.processingUnit)
                        ?.findLast {
                            it.typeDiscrepancies == typeDiscrepancies
                                    && it.manufactureCode == manufactureCode
                                    && it.shelfLifeDate == shelfLifeDate
                                    && it.shelfLifeTime == shelfLifeTime
                                    && it.productionDate == productionDate
                        }

        foundDiscrepancy =
                foundDiscrepancy
                        ?.copy(partySign = partySignsType)
                        ?: PartySignsOfZBatches(
                                processingUnit = productInfo.processingUnit,
                                materialNumber = productInfo.materialNumber,
                                batchNumber = "",
                                typeDiscrepancies = typeDiscrepancies,
                                manufactureCode = manufactureCode,
                                shelfLifeDate = shelfLifeDate,
                                shelfLifeTime = shelfLifeTime,
                                productionDate = productionDate,
                                partySign = partySignsType
                        )

        taskRepository
                ?.getZBatchesDiscrepancies()
                ?.changePartySign(foundDiscrepancy)
    }

}