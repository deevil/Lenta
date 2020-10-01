package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS
import com.lenta.shared.di.AppScope
import javax.inject.Inject

@AppScope
class ProcessZBatchesPGEService
@Inject constructor() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    private lateinit var productInfo: TaskProductInfo
    val taskRepository by lazy { taskManager.getReceivingTask()?.taskRepository }

    fun newProcessZBatchesPGEService(initProductInfo: TaskProductInfo): ProcessZBatchesPGEService? {
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

    fun checkParam(param: String): Boolean {
        return !taskRepository
                ?.getProductsDiscrepancies()
                ?.findProductDiscrepanciesOfProduct(productInfo)
                ?.filter { it.typeDiscrepancies == param }
                .isNullOrEmpty()
    }

    fun removeDiscrepancyFromProduct(
            typeDiscrepancies: String,
            processingUnitNumber: String,
            manufactureCode: String,
            shelfLifeDate: String,
            shelfLifeTime: String,
            productionDate: String
    ) {
        taskRepository
                ?.getProductsDiscrepancies()
                ?.deleteProductDiscrepancyOfProcessingUnit(
                        materialNumber = productInfo.materialNumber,
                        typeDiscrepancies = typeDiscrepancies,
                        processingUnitNumber = processingUnitNumber
                )

        val foundZBatchesDiscrepancy =
                taskRepository
                        ?.getZBatchesDiscrepancies()
                        ?.findZBatchDiscrepanciesOfProduct(productInfo.materialNumber)
                        ?.findLast {
                            it.typeDiscrepancies == typeDiscrepancies
                                    && it.processingUnit == processingUnitNumber
                                    && it.manufactureCode == manufactureCode
                                    && it.shelfLifeDate == shelfLifeDate
                                    && it.shelfLifeTime == shelfLifeTime
                        }

        foundZBatchesDiscrepancy?.let {
            taskRepository?.getZBatchesDiscrepancies()?.deleteZBatchDiscrepancies(it)
        }

        val foundPartySign =
                taskRepository
                        ?.getZBatchesDiscrepancies()
                        ?.findPartySignsOfProduct(productInfo.materialNumber, processingUnitNumber)
                        ?.findLast {
                            it.typeDiscrepancies == typeDiscrepancies
                                    && it.manufactureCode == manufactureCode
                                    && it.shelfLifeDate == shelfLifeDate
                                    && it.shelfLifeTime == shelfLifeTime
                                    && it.productionDate == productionDate
                        }

        foundPartySign?.let {
            taskRepository?.getZBatchesDiscrepancies()?.deletePartySignOfZBatches(it)
        }
    }

    fun addWithoutUnderload(
            typeDiscrepancies: String,
            count: String,
            manufactureCode: String,
            shelfLifeDate: String,
            shelfLifeTime: String,
            productionDate: String,
            processingUnit: String,
            partySignsType: PartySignsTypeOfZBatches,
            isShelfLifeObtainedFromEWM: Boolean
    ) {
        val productDiscrepancy =
                taskRepository
                        ?.getProductsDiscrepancies()
                        ?.findProductDiscrepanciesOfProductOfProcessingUnit(productInfo.materialNumber, processingUnit)
                        ?.findLast { it.typeDiscrepancies == typeDiscrepancies }

        if (productDiscrepancy != null) {
            taskRepository
                    ?.getProductsDiscrepancies()
                    ?.changeProductDiscrepancyOfProcessingUnit(productDiscrepancy.copy(numberDiscrepancies = count))
        } else {
            add(count, typeDiscrepancies, manufactureCode, shelfLifeDate, shelfLifeTime, productionDate, processingUnit, partySignsType, isShelfLifeObtainedFromEWM)
        }
    }

    fun overLimit(count: Double): Boolean {
        val totalCount = getCountAcceptOfProduct() + getCountRefusalOfProduct() + count

        return productInfo.origQuantity.toDouble() < totalCount
    }

    fun add(count: String,
            typeDiscrepancies: String,
            manufactureCode: String,
            shelfLifeDate: String,
            shelfLifeTime: String,
            productionDate: String,
            processingUnit: String,
            partySignsType: PartySignsTypeOfZBatches,
            isShelfLifeObtainedFromEWM: Boolean
    ) {
        changeProductDiscrepancy(count, typeDiscrepancies, processingUnit)

        if ((typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM || typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS)
                || isShelfLifeObtainedFromEWM) {
            changeZBatchDiscrepancy(count, typeDiscrepancies, manufactureCode, shelfLifeDate, shelfLifeTime, processingUnit)
            changePartySign(typeDiscrepancies, manufactureCode, shelfLifeDate, shelfLifeTime, productionDate, processingUnit, partySignsType)
        }
    }

    private fun changeProductDiscrepancy(count: String, typeDiscrepancies: String, processingUnit: String) {
        val countAdd = getCountOfDiscrepanciesOfProduct(typeDiscrepancies, processingUnit) + count.toDouble()

        var foundDiscrepancy =
                taskRepository
                        ?.getProductsDiscrepancies()
                        ?.findProductDiscrepanciesOfProductOfProcessingUnit(productInfo.materialNumber, processingUnit)
                        ?.findLast { it.typeDiscrepancies == typeDiscrepancies }

        foundDiscrepancy =
                foundDiscrepancy
                        ?.copy(numberDiscrepancies = countAdd.toString())
                        ?: TaskProductDiscrepancies(
                                materialNumber = productInfo.materialNumber,
                                processingUnitNumber = processingUnit,
                                numberDiscrepancies = countAdd.toString(),
                                uom = productInfo.uom,
                                typeDiscrepancies = typeDiscrepancies,
                                isNotEdit = false,
                                isNew = false,
                                notEditNumberDiscrepancies = ""
                        )

        taskRepository
                ?.getProductsDiscrepancies()
                ?.changeProductDiscrepancyOfProcessingUnit(foundDiscrepancy)
    }

    private fun changeZBatchDiscrepancy(count: String, typeDiscrepancies: String, manufactureCode: String, shelfLifeDate: String, shelfLifeTime: String, processingUnit: String) {
        val countAdd = getCountOfDiscrepanciesOfProductOfZBatch(typeDiscrepancies, manufactureCode, shelfLifeDate, shelfLifeTime) + count.toDouble()

        var foundDiscrepancy =
                taskRepository
                        ?.getZBatchesDiscrepancies()
                        ?.findZBatchDiscrepanciesOfProduct(productInfo.materialNumber)
                        ?.findLast {
                            it.typeDiscrepancies == typeDiscrepancies
                                    && it.processingUnit == processingUnit
                                    && it.manufactureCode == manufactureCode
                                    && it.shelfLifeDate == shelfLifeDate
                                    && it.shelfLifeTime == shelfLifeTime
                        }

        foundDiscrepancy =
                foundDiscrepancy
                        ?.copy(numberDiscrepancies = countAdd.toString())
                        ?: TaskZBatchesDiscrepancies(
                                processingUnit = processingUnit,
                                materialNumber = productInfo.materialNumber,
                                batchNumber = "",
                                numberDiscrepancies = countAdd.toString(),
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

    private fun changePartySign(
            typeDiscrepancies: String,
            manufactureCode: String,
            shelfLifeDate: String,
            shelfLifeTime: String,
            productionDate: String,
            processingUnit: String,
            partySignsType: PartySignsTypeOfZBatches
    ) {

        var foundPartySign =
                taskRepository
                        ?.getZBatchesDiscrepancies()
                        ?.findPartySignsOfProduct(productInfo.materialNumber, processingUnit)
                        ?.findLast {
                            it.typeDiscrepancies == typeDiscrepancies
                                    && it.manufactureCode == manufactureCode
                                    && it.shelfLifeDate == shelfLifeDate
                                    && it.shelfLifeTime == shelfLifeTime
                                    && it.productionDate == productionDate
                        }

        foundPartySign =
                foundPartySign
                        ?.copy(partySign = partySignsType)
                        ?: PartySignsOfZBatches(
                                processingUnit = processingUnit,
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
                ?.changePartySign(foundPartySign)
    }

    fun countEqualOrigQuantityPGE (count: Double) : Boolean {
        return productInfo.origQuantity.toDouble() == ((taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProductPGE(productInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProductPGE(productInfo) ?: 0.0) + count)
    }

    fun getQuantityAllCategoryPGE(count: Double) : Double {
        return ((taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProductPGE(productInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProductPGE(productInfo) ?: 0.0) + count)
    }

    fun getOpenQuantityPGE(paramGrwOlGrundcat: String, paramGrwUlGrundcat: String) : Double {
        val countCategoryUnderload = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.findLast {
            it.typeDiscrepancies == paramGrwUlGrundcat
        }?.numberDiscrepancies?.toDouble() ?: 0.0

        val countCategorySurplus = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.findLast {
            it.typeDiscrepancies == paramGrwOlGrundcat
        }?.numberDiscrepancies?.toDouble() ?: 0.0

        val countAllCategory =
                (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProductPGE(productInfo) ?: 0.0) + (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProductPGE(productInfo) ?: 0.0)

        return productInfo.orderQuantity.toDouble() - countCategoryUnderload - countAllCategory + countCategorySurplus
    }

    fun getRoundingQuantityPGE () : Double {
        return productInfo.orderQuantity.toDouble() - ((taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProductPGE(productInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProductPGE(productInfo) ?: 0.0))
    }

    fun countWithoutParamGrwUlGrundcatPGE(paramGrwOlGrundcat: String, paramGrwUlGrundcat: String) : Double {
        return productInfo.orderQuantity.toDouble() - getOpenQuantityPGE(paramGrwOlGrundcat, paramGrwUlGrundcat)
    }

    fun countNormAndParamMoreOrderQuantityPGE (paramGrwOlGrundcat: String, count: Double) : Boolean {
        var countNormAndParam =
                taskManager
                        .getReceivingTask()
                        ?.taskRepository
                        ?.getProductsDiscrepancies()
                        ?.findProductDiscrepanciesOfProduct(productInfo)
                        ?.filter {
                            (it.typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM || it.typeDiscrepancies == paramGrwOlGrundcat)
                                    && it.processingUnitNumber == productInfo.processingUnit
                        }?.sumByDouble {
                            it.numberDiscrepancies.toDouble()
                        } ?: 0.0

        countNormAndParam += count
        return countNormAndParam > productInfo.orderQuantity.toDouble()
    }

    fun addCountMoreCargoUnit(
            paramGrwOlGrundcat: String,
            count: Double,
            processingUnit: String,
            manufactureCode: String,
            shelfLifeDate: String,
            shelfLifeTime: String,
            productionDate: String,
            partySignsType: PartySignsTypeOfZBatches,
            isShelfLifeObtainedFromEWM: Boolean
    ) {
        var countNormAndParam =
                taskManager
                        .getReceivingTask()
                        ?.taskRepository
                        ?.getProductsDiscrepancies()
                        ?.findProductDiscrepanciesOfProduct(productInfo)
                        ?.filter {
                            it.typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM
                                    || it.typeDiscrepancies == paramGrwOlGrundcat
                        }?.sumByDouble {
                            it.numberDiscrepancies.toDouble()
                        } ?: 0.0

        countNormAndParam += count

        taskManager
                .getReceivingTask()
                ?.taskRepository
                ?.getProductsDiscrepancies()
                ?.deleteProductDiscrepancyOfProcessingUnit(
                        materialNumber =  productInfo.materialNumber,
                        typeDiscrepancies = TYPE_DISCREPANCIES_QUALITY_NORM,
                        processingUnitNumber = processingUnit
                )

        var foundZBatchesDiscrepancy =
                taskRepository
                        ?.getZBatchesDiscrepancies()
                        ?.findZBatchDiscrepanciesOfProduct(productInfo.materialNumber)
                        ?.findLast {
                            it.typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM
                                    && it.processingUnit == processingUnit
                                    && it.manufactureCode == manufactureCode
                                    && it.shelfLifeDate == shelfLifeDate
                                    && it.shelfLifeTime == shelfLifeTime
                        }

        foundZBatchesDiscrepancy?.let {
            taskRepository?.getZBatchesDiscrepancies()?.deleteZBatchDiscrepancies(it)
        }

        var foundPartySign =
                taskRepository
                        ?.getZBatchesDiscrepancies()
                        ?.findPartySignsOfProduct(productInfo.materialNumber, processingUnit)
                        ?.findLast {
                            it.typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM
                                    && it.manufactureCode == manufactureCode
                                    && it.shelfLifeDate == shelfLifeDate
                                    && it.shelfLifeTime == shelfLifeTime
                                    && it.productionDate == productionDate
                        }

        foundPartySign?.let {
            taskRepository?.getZBatchesDiscrepancies()?.deletePartySignOfZBatches(it)
        }

        add(count = productInfo.orderQuantity,
                typeDiscrepancies = TYPE_DISCREPANCIES_QUALITY_NORM,
                manufactureCode = manufactureCode,
                shelfLifeDate = shelfLifeDate,
                shelfLifeTime = shelfLifeTime,
                productionDate = productionDate,
                processingUnit = processingUnit,
                partySignsType = partySignsType,
                isShelfLifeObtainedFromEWM = isShelfLifeObtainedFromEWM
        )

        taskManager
                .getReceivingTask()
                ?.taskRepository
                ?.getProductsDiscrepancies()
                ?.deleteProductDiscrepancyOfProcessingUnit(
                        materialNumber =  productInfo.materialNumber,
                        typeDiscrepancies = paramGrwOlGrundcat,
                        processingUnitNumber = processingUnit
                )

        foundZBatchesDiscrepancy =
                taskRepository
                        ?.getZBatchesDiscrepancies()
                        ?.findZBatchDiscrepanciesOfProduct(productInfo.materialNumber)
                        ?.findLast {
                            it.typeDiscrepancies == paramGrwOlGrundcat
                                    && it.processingUnit == processingUnit
                                    && it.manufactureCode == manufactureCode
                                    && it.shelfLifeDate == shelfLifeDate
                                    && it.shelfLifeTime == shelfLifeTime
                        }

        foundZBatchesDiscrepancy?.let {
            taskRepository?.getZBatchesDiscrepancies()?.deleteZBatchDiscrepancies(it)
        }

        foundPartySign =
                taskRepository
                        ?.getZBatchesDiscrepancies()
                        ?.findPartySignsOfProduct(productInfo.materialNumber, processingUnit)
                        ?.findLast {
                            it.typeDiscrepancies == paramGrwOlGrundcat
                                    && it.manufactureCode == manufactureCode
                                    && it.shelfLifeDate == shelfLifeDate
                                    && it.shelfLifeTime == shelfLifeTime
                                    && it.productionDate == productionDate
                        }

        foundPartySign?.let {
            taskRepository?.getZBatchesDiscrepancies()?.deletePartySignOfZBatches(it)
        }

        add(count = (countNormAndParam - productInfo.orderQuantity.toDouble()).toString(),
                typeDiscrepancies = paramGrwOlGrundcat,
                manufactureCode = manufactureCode,
                shelfLifeDate = shelfLifeDate,
                shelfLifeTime = shelfLifeTime,
                productionDate = productionDate,
                processingUnit = processingUnit,
                partySignsType = partySignsType,
                isShelfLifeObtainedFromEWM = isShelfLifeObtainedFromEWM
        )
    }

}