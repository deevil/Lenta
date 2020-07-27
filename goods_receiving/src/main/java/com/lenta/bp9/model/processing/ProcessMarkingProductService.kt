package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.di.AppScope
import com.lenta.shared.models.core.ProductType
import javax.inject.Inject

@AppScope
class ProcessMarkingProductService
@Inject constructor() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var dataBase: IDataBaseRepo

    private lateinit var productInfo: TaskProductInfo
    private val blocks: ArrayList<TaskBlockInfo> = ArrayList()
    private val currentBlocksDiscrepancies: ArrayList<TaskBlockDiscrepancies> = ArrayList()
    private val currentGtin: ArrayList<String> = ArrayList()

    fun initProduct(productInfo: TaskProductInfo) {
        this.productInfo = productInfo.copy()
    }

    fun newProcessMarkingProductService(productInfo: TaskProductInfo): ProcessMarkingProductService? {
        return if (productInfo.type == ProductType.General) {
            this.productInfo = productInfo.copy()
            currentGtin.clear()
            blocks.clear()
            taskManager.getReceivingTask()
                    ?.getProcessedBlocks()
                    ?.map {
                        blocks.add(it.copy())
                    }
            currentBlocksDiscrepancies.clear()
            taskManager.getReceivingTask()
                    ?.taskRepository
                    ?.getBlocksDiscrepancies()
                    ?.findBlocksDiscrepanciesOfProduct(productInfo)
                    ?.map {
                        currentBlocksDiscrepancies.add(it.copy())
                    }
            this
        } else null
    }

    fun apply() {
        if (currentBlocksDiscrepancies.isNotEmpty()) {
            currentBlocksDiscrepancies.map {
                taskManager.getReceivingTask()
                        ?.taskRepository
                        ?.getBlocksDiscrepancies()
                        ?.changeBlockDiscrepancy(it)
            }
        }
    }

    fun addProduct(count: String, typeDiscrepancies: String) {
        val countAdd =
                if (typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
                    getCountAcceptOfProduct() + count.toDouble()
                } else {
                    getCountOfDiscrepanciesOfProduct(typeDiscrepancies) + count.toDouble()
                }

        //добавляем кол-во по расхождению для продукта
        var foundDiscrepancy =
                taskManager.getReceivingTask()
                        ?.taskRepository
                        ?.getProductsDiscrepancies()
                        ?.findProductDiscrepanciesOfProduct(productInfo)
                        ?.findLast {
                            it.typeDiscrepancies == typeDiscrepancies
                        }

        foundDiscrepancy = foundDiscrepancy?.copy(numberDiscrepancies = countAdd.toString())
                ?: TaskProductDiscrepancies(
                        materialNumber = productInfo.materialNumber,
                        processingUnitNumber = productInfo.processingUnit,
                        numberDiscrepancies = countAdd.toString(),
                        uom = productInfo.uom,
                        typeDiscrepancies = typeDiscrepancies,
                        isNotEdit = false,
                        isNew = false,
                        notEditNumberDiscrepancies = ""
                )

        taskManager.getReceivingTask()
                ?.taskRepository
                ?.getProductsDiscrepancies()
                ?.changeProductDiscrepancy(foundDiscrepancy)
    }

    fun addBlocksDiscrepancies(blockInfo: TaskBlockInfo, typeDiscrepancies: String, isScan: Boolean) {
        var foundBlockDiscrepancy =
                currentBlocksDiscrepancies.findLast {
                    it.blockNumber == blockInfo.blockNumber
                }

        foundBlockDiscrepancy = foundBlockDiscrepancy?.copy(typeDiscrepancies = typeDiscrepancies)
                ?: TaskBlockDiscrepancies(
                        processingUnitNumber = blockInfo.processingUnitNumber,
                        materialNumber = blockInfo.materialNumber,
                        blockNumber = blockInfo.blockNumber,
                        boxNumber = "",
                        typeDiscrepancies = typeDiscrepancies,
                        isScan = isScan,
                        isMsc = false,
                        isUnknown = false,
                        isGrayZone = false
                )

        currentBlocksDiscrepancies.asSequence()
                .map { it }
                .filter { block ->
                    if (block.blockNumber == blockInfo.blockNumber) {
                        currentBlocksDiscrepancies.remove(block)
                        return@filter true
                    }
                    return@filter false
                }

        currentBlocksDiscrepancies.add(foundBlockDiscrepancy)
    }

    fun getLastScannedBlock(): TaskBlockDiscrepancies? {
        return if (currentBlocksDiscrepancies.isNullOrEmpty()) {
            null
        } else {
            currentBlocksDiscrepancies.last()
        }
    }

    fun addGtin(gtinCode: String) {
        var index = -1
        for (i in currentGtin.indices) {
            if (gtinCode == currentGtin[i]) {
                index = i
            }
        }

        if (index == -1) {
            currentGtin.add(gtinCode)
        }
    }

    fun getLastScannedGtin(): String? {
        return if (currentGtin.isNullOrEmpty()) {
            null
        } else {
            currentGtin.last()
        }
    }

    fun replaceLastGtin(gtinCode: String) {
        currentGtin.map { it }
                .findLast { lastGtin ->
                    if (lastGtin == gtinCode) {
                        currentGtin.remove(lastGtin)
                        return@findLast true
                    }
                    return@findLast false
                }

        addGtin(gtinCode)
    }

    fun getCountProcessedBlock(): Int {
        return currentBlocksDiscrepancies.size
    }

    fun getRefusalCountProcessedBlock(): Double {
        return currentBlocksDiscrepancies
                .filter {
                    it.typeDiscrepancies != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                }
                .size
                .toDouble()
    }

    fun rollbackScannedBlock() {
        val block = currentBlocksDiscrepancies.last()
        currentBlocksDiscrepancies.remove(block)
    }

    fun rollbackScannedGtin() {
        val gtin = currentGtin.last()
        currentGtin.remove(gtin)
    }

    fun overLimit(count: Double): Boolean {
        return productInfo.origQuantity.toDouble() < (getCountAcceptOfProduct() + getCountRefusalOfProduct() + count)
    }

    fun searchBlock(blockNumber: String): TaskBlockInfo? {
        return blocks.findLast {
            it.blockNumber == blockNumber
        }
    }

    fun blockIsAlreadyProcessed(code: String): Boolean {
        return currentBlocksDiscrepancies.any {
            it.blockNumber == code && it.isScan
        }
    }

    private fun getCountOfDiscrepanciesOfProduct(typeDiscrepancies: String): Double {
        return taskManager.getReceivingTask()
                ?.taskRepository
                ?.getProductsDiscrepancies()
                ?.findProductDiscrepanciesOfProduct(productInfo)
                ?.filter { productDiscrepancies ->
                    productDiscrepancies.typeDiscrepancies == typeDiscrepancies
                }?.sumByDouble {
                    it.numberDiscrepancies.toDouble()
                }
                ?: 0.0
    }

    fun getCountAcceptOfProduct(): Double {
        return taskManager.getReceivingTask()
                ?.taskRepository
                ?.getProductsDiscrepancies()
                ?.findProductDiscrepanciesOfProduct(productInfo)
                ?.filter { productDiscrepancies ->
                    productDiscrepancies.typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                }?.sumByDouble {
                    it.numberDiscrepancies.toDouble()
                }
                ?: 0.0
    }

    fun getCountRefusalOfProduct(): Double {
        return taskManager.getReceivingTask()
                ?.taskRepository
                ?.getProductsDiscrepancies()
                ?.findProductDiscrepanciesOfProduct(productInfo)
                ?.filter { productDiscrepancies ->
                    productDiscrepancies.typeDiscrepancies != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                }
                ?.sumByDouble {
                    it.numberDiscrepancies.toDouble()
                }
                ?: 0.0
    }

    fun denialOfFullProductAcceptance(typeDiscrepancies: String) {
        //https://trello.com/c/vcymT9Kp
        //отмечаем все блоки/марки для продукта категорией для брака из параметра GRZ_GRUND_MARK
        blocks.asSequence()
                .filter { block ->
                    block.materialNumber == productInfo.materialNumber
                }
                .map { blockInfo ->
                    addBlocksDiscrepancies(
                            blockInfo = blockInfo,
                            typeDiscrepancies = typeDiscrepancies,
                            isScan = false //передаем false, т.к. эта ф-ция (denialOfFullProductAcceptance) вызывается с экрана Обнаружены расхождения по клику на блок на вкладке Не обработаны
                    )
                    apply()
                }

        //отмечаем продукт
        addProduct(productInfo.origQuantity, typeDiscrepancies)
    }

    fun refusalToAcceptPartlyByProduct(typeDiscrepancies: String) {
        //https://trello.com/c/vcymT9Kp
        //отмечаем все не обработанные блоки/марки для продукта категорией для брака из параметра GRZ_GRUND_MARK
        blocks.asSequence()
                .filter { block ->
                    block.materialNumber == productInfo.materialNumber
                            && currentBlocksDiscrepancies.findLast { it.blockNumber == block.blockNumber } == null
                }.map { blockInfo ->
                    addBlocksDiscrepancies(
                            blockInfo = blockInfo,
                            typeDiscrepancies = typeDiscrepancies,
                            isScan = false //передаем false, т.к. эта ф-ция (denialOfFullProductAcceptance) вызывается с экрана Обнаружены расхождения по клику на блок на вкладке Не обработаны
                    )
                    apply()
                }

        //отмечаем продукт
        addProduct((productInfo.origQuantity.toDouble() - getCountAcceptOfProduct() - getCountRefusalOfProduct()).toString(), typeDiscrepancies)
    }

    fun modifications(): Boolean {
        return currentBlocksDiscrepancies !=
                taskManager.getReceivingTask()
                        ?.taskRepository
                        ?.getBlocksDiscrepancies()
                        ?.getBlocksDiscrepancies()
    }

    fun clearModifications() {
        currentGtin.clear()
        blocks.clear()
        taskManager.getReceivingTask()
                ?.getProcessedBlocks()
                ?.map {
                    blocks.add(it.copy())
                }
        currentBlocksDiscrepancies.clear()
        taskManager.getReceivingTask()
                ?.taskRepository
                ?.getBlocksDiscrepancies()
                ?.findBlocksDiscrepanciesOfProduct(productInfo)
                ?.map {
                    currentBlocksDiscrepancies.add(it.copy())
                }
    }

}