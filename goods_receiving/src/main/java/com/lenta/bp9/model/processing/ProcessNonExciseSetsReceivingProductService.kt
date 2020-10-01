package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.*
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.di.AppScope
import com.lenta.shared.models.core.ProductType
import javax.inject.Inject

@AppScope
class ProcessNonExciseSetsReceivingProductService
@Inject constructor() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var dataBase: IDataBaseRepo

    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    private lateinit var productInfo: TaskProductInfo
    private val currentComponentsDiscrepancies: ArrayList<TaskProductDiscrepancies> = ArrayList()
    private val currentBatchesDiscrepancies: ArrayList<TaskBatchesDiscrepancies> = ArrayList()

    fun newProcessNonExciseSetsReceivingProductService(productInfo: TaskProductInfo): ProcessNonExciseSetsReceivingProductService? {
        return if (productInfo.type == ProductType.NonExciseAlcohol && productInfo.isSet) {
            this.productInfo = productInfo.copy()
            currentComponentsDiscrepancies.clear()
            currentBatchesDiscrepancies.clear()
            this
        } else null
    }

    private fun getCountOfDiscrepanciesOfSet(typeDiscrepancies: String, processingUnitNumber: String): Double {
        return taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountOfDiscrepanciesOfProductOfProcessingUnit(productInfo, typeDiscrepancies, processingUnitNumber)
                ?: 0.0
    }

    fun getCountDiscrepanciesOfComponent(componentNumber: String): Double {
        return currentComponentsDiscrepancies.filter { unitInfo ->
            unitInfo.materialNumber == componentNumber
        }.sumByDouble {
            it.numberDiscrepancies.toDouble()
        }
    }

    private fun getCountOfDiscrepanciesOfBatch(batchInfo: TaskBatchInfo, typeDiscrepancies: String): Double {
        return taskManager.getReceivingTask()!!.taskRepository.getBatchesDiscrepancies().getCountOfDiscrepanciesOfBatch(batchInfo, typeDiscrepancies)
    }

    fun apply(count: String, typeDiscrepancies: String) {
        val countAdd: Double = getCountOfDiscrepanciesOfSet(typeDiscrepancies, productInfo.processingUnit) + count.toDouble()
        val foundDiscrepancy = taskManager
                .getReceivingTask()
                ?.taskRepository
                ?.getProductsDiscrepancies()
                ?.findProductDiscrepanciesOfProduct(productInfo)
                ?.findLast {
                    it.materialNumber == productInfo.materialNumber && it.typeDiscrepancies == typeDiscrepancies
                }

        if (foundDiscrepancy == null) {
            taskManager
                    .getReceivingTask()
                    ?.taskRepository
                    ?.getProductsDiscrepancies()
                    ?.changeProductDiscrepancyOfProcessingUnit(
                            TaskProductDiscrepancies(
                                    materialNumber = productInfo.materialNumber,
                                    processingUnitNumber = productInfo.processingUnit,
                                    numberDiscrepancies = countAdd.toString(),
                                    uom = productInfo.uom,
                                    typeDiscrepancies = typeDiscrepancies,
                                    isNotEdit = false,
                                    isNew = productInfo.isGoodsAddedAsSurplus,
                                    notEditNumberDiscrepancies = ""
                            )
                    )
        } else {
            taskManager
                    .getReceivingTask()
                    ?.taskRepository
                    ?.getProductsDiscrepancies()
                    ?.changeProductDiscrepancyOfProcessingUnit(foundDiscrepancy.copy(numberDiscrepancies = countAdd.toString()))
        }

        if (currentComponentsDiscrepancies.isNotEmpty()) {
            currentComponentsDiscrepancies.map {
                taskManager
                        .getReceivingTask()
                        ?.taskRepository
                        ?.getProductsDiscrepancies()
                        ?.changeProductDiscrepancyOfProcessingUnit(it)
            }
        }

        if (currentBatchesDiscrepancies.isNotEmpty()) {
            currentBatchesDiscrepancies.map {
                taskManager
                        .getReceivingTask()
                        ?.taskRepository
                        ?.getBatchesDiscrepancies()
                        ?.changeBatchDiscrepancy(it)
            }
        }
    }

    fun addCurrentComponent(count: String, typeDiscrepancies: String, componentInfo: TaskSetsInfo, batchInfo: TaskBatchInfo) {
        var foundDiscrepancy = currentComponentsDiscrepancies.findLast {
            it.materialNumber == componentInfo.componentNumber && it.typeDiscrepancies == typeDiscrepancies
        }
        val countAdd = (foundDiscrepancy?.numberDiscrepancies?.toDoubleOrNull()
                ?: 0.0) + count.toDouble()


        foundDiscrepancy = foundDiscrepancy?.copy(numberDiscrepancies = countAdd.toString())
                ?: TaskProductDiscrepancies(
                        materialNumber = componentInfo.componentNumber,
                        processingUnitNumber = "",
                        numberDiscrepancies = countAdd.toString(),
                        uom = componentInfo.uom,
                        typeDiscrepancies = typeDiscrepancies,
                        isNotEdit = false,
                        isNew = false,
                        notEditNumberDiscrepancies = ""
                )

        currentComponentsDiscrepancies.map { it }.filter { unitInfo ->
            if (unitInfo.materialNumber == componentInfo.componentNumber && unitInfo.typeDiscrepancies == typeDiscrepancies) {
                currentComponentsDiscrepancies.remove(unitInfo)
                return@filter true
            }
            return@filter false
        }

        currentComponentsDiscrepancies.add(foundDiscrepancy)

        addBatch(count, typeDiscrepancies, componentInfo, batchInfo)
    }

    private fun addBatch(count: String, typeDiscrepancies: String, componentInfo: TaskSetsInfo, batchInfo: TaskBatchInfo) {
        var foundBatchDiscrepancy = currentBatchesDiscrepancies.findLast {
            it.materialNumber == batchInfo.materialNumber &&
                    it.typeDiscrepancies == typeDiscrepancies &&
                    it.processingUnitNumber == batchInfo.processingUnitNumber &&
                    it.batchNumber == batchInfo.batchNumber
        }
        val countAdd = (foundBatchDiscrepancy?.numberDiscrepancies?.toDoubleOrNull()
                ?: 0.0) + count.toDouble()


        foundBatchDiscrepancy = foundBatchDiscrepancy?.copy(numberDiscrepancies = countAdd.toString())
                ?: TaskBatchesDiscrepancies(
                        materialNumber = batchInfo.materialNumber,
                        processingUnitNumber = batchInfo.processingUnitNumber,
                        batchNumber = batchInfo.batchNumber,
                        numberDiscrepancies = countAdd.toString(),
                        uom = componentInfo.uom,
                        typeDiscrepancies = typeDiscrepancies,
                        isNotEdit = false,
                        isNew = false,
                        setMaterialNumber = batchInfo.setMaterialNumber,
                        egais = batchInfo.egais,
                        bottlingDate = batchInfo.bottlingDate,
                        notEditNumberDiscrepancies = ""
                )

        currentBatchesDiscrepancies.map { it }.filter { unitInfo ->
            if (unitInfo.materialNumber == batchInfo.materialNumber &&
                    unitInfo.typeDiscrepancies == typeDiscrepancies &&
                    unitInfo.processingUnitNumber == batchInfo.processingUnitNumber &&
                    unitInfo.batchNumber == batchInfo.batchNumber) {
                currentBatchesDiscrepancies.remove(unitInfo)
                return@filter true
            }
            return@filter false
        }

        currentBatchesDiscrepancies.add(foundBatchDiscrepancy)

    }

    fun cleanCurrentComponent(componentNumber: String) {
        currentComponentsDiscrepancies.map { it }.filter { unitInfo ->
            if (unitInfo.materialNumber == componentNumber) {
                currentComponentsDiscrepancies.remove(unitInfo)
                return@filter true
            }
            return@filter false
        }
    }

    fun clearCurrentComponent() {
        currentComponentsDiscrepancies.clear()
    }

}