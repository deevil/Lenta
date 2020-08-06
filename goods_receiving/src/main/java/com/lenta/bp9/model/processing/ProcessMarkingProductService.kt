package com.lenta.bp9.model.processing

import com.lenta.bp9.features.goods_information.marking.TypeLastStampScanned
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.di.AppScope
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.utilities.extentions.removeItemFromListWithPredicate
import javax.inject.Inject

@AppScope
class ProcessMarkingProductService
@Inject constructor() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var dataBase: IDataBaseRepo

    private lateinit var productInfo: TaskProductInfo
    private var isBlockMode: Boolean = false
    private val blocks: ArrayList<TaskBlockInfo> = ArrayList()
    private val currentBlocksDiscrepancies: ArrayList<BlocksDiscrepanciesInfo> = ArrayList()
    private val currentGtin: ArrayList<String> = ArrayList()
    private val currentScannedTypesStamps: ArrayList<TypeLastStampScanned> = ArrayList()

    fun initProduct(inputProductInfo: TaskProductInfo) {
        this.productInfo = inputProductInfo.copy()
    }

    fun newProcessMarkingProductService(inputProductInfo: TaskProductInfo): ProcessMarkingProductService? {
        return this
                .takeIf { inputProductInfo.type == ProductType.General }
                ?.apply {
                    this.productInfo = inputProductInfo.copy()
                    isBlockMode =
                            (this.productInfo.purchaseOrderUnits.code == UNIT_ST
                                    || this.productInfo.purchaseOrderUnits.code == UNIT_P09)
                                    && this.productInfo.isCountingBoxes == false
                    currentGtin.clear()
                    currentScannedTypesStamps.clear()
                    blocks.clear()
                    taskManager.getReceivingTask()
                            ?.getProcessedBlocks()
                            ?.mapTo(blocks) {
                                it.copy()
                            }
                    currentBlocksDiscrepancies.clear()
                    taskManager.getReceivingTask()
                            ?.taskRepository
                            ?.getBlocksDiscrepancies()
                            ?.findBlocksDiscrepanciesOfProduct(productInfo)
                            ?.mapTo(currentBlocksDiscrepancies) {
                                BlocksDiscrepanciesInfo(it.copy(), true)
                            }
                }
    }

    fun apply() {
        currentBlocksDiscrepancies
                .takeIf {
                    it.isNotEmpty()
                }
                ?.run {
                    this.asSequence()
                            .filter { blockDiscrepanciesInfo ->
                                blockDiscrepanciesInfo.isGtinControlPassed
                            }.map { block ->
                                taskManager
                                        .getReceivingTask()
                                        ?.taskRepository
                                        ?.getBlocksDiscrepancies()
                                        ?.changeBlockDiscrepancy(block.blockDiscrepancies)
                            }
                            .toList()
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

        taskManager
                .getReceivingTask()
                ?.taskRepository
                ?.getProductsDiscrepancies()
                ?.changeProductDiscrepancy(foundDiscrepancy)
    }

    fun addBlockDiscrepancies(blockInfo: TaskBlockInfo, typeDiscrepancies: String, isScan: Boolean, isGtinControlPassed: Boolean) {
        var foundBlockDiscrepancy =
                currentBlocksDiscrepancies
                        .findLast {
                            it.blockDiscrepancies.blockNumber == blockInfo.blockNumber
                        }

        foundBlockDiscrepancy =
                foundBlockDiscrepancy
                        ?.let {
                            BlocksDiscrepanciesInfo(
                                    blockDiscrepancies = it.blockDiscrepancies.copy(typeDiscrepancies = typeDiscrepancies),
                                    isGtinControlPassed = it.isGtinControlPassed
                            )
                        }
                        ?: BlocksDiscrepanciesInfo(
                                blockDiscrepancies = TaskBlockDiscrepancies(
                                        processingUnitNumber = blockInfo.processingUnitNumber,
                                        materialNumber = blockInfo.materialNumber,
                                        blockNumber = blockInfo.blockNumber,
                                        boxNumber = "",
                                        typeDiscrepancies = typeDiscrepancies,
                                        isScan = isScan,
                                        isMsc = false,
                                        isUnknown = false,
                                        isGrayZone = false
                                ),
                                isGtinControlPassed = isGtinControlPassed
                        )

        currentBlocksDiscrepancies
                .map { it }
                .filter { block ->
                    if (block.blockDiscrepancies.blockNumber == blockInfo.blockNumber) {
                        currentBlocksDiscrepancies.remove(block)
                        return@filter true
                    }
                    return@filter false
                }

        currentBlocksDiscrepancies.add(foundBlockDiscrepancy)

        addTypeLastStampScanned(TypeLastStampScanned.BLOCK)
    }

    fun markPassageControlBlock(blockNumber: String) {
        val blockDiscrepancies =
                currentBlocksDiscrepancies
                        .findLast {
                            it.blockDiscrepancies.blockNumber == blockNumber
                        }?.blockDiscrepancies

        blockDiscrepancies?.let {
            for (i in currentBlocksDiscrepancies.indices) {
                if (currentBlocksDiscrepancies[i].blockDiscrepancies.blockNumber == blockNumber) {
                    currentBlocksDiscrepancies[i] = BlocksDiscrepanciesInfo(it, true)
                    break
                }
            }
        }
    }

    fun addAllUntreatedBlocksAsDefect(typeDiscrepancies: String) {
        //отмечаем все не обработанные блоки/марки для продукта категорией выбранной категорией для брака
        blocks
                .filter { block ->
                    block.materialNumber == productInfo.materialNumber
                            && currentBlocksDiscrepancies.findLast { it.blockDiscrepancies.blockNumber == block.blockNumber } == null
                }.map { blockInfo ->
                    addBlockDiscrepancies(
                            blockInfo = blockInfo,
                            typeDiscrepancies = typeDiscrepancies,
                            isScan = false, //передаем false, т.к. эта ф-ция вызывается при сохранении всех необработанных блоков в брак без сканирования
                            isGtinControlPassed = true
                    )
                }
    }

    fun getTotalScannedBlocks(): Int {
        return currentBlocksDiscrepancies.size
    }

    fun getLastScannedBlock(): TaskBlockInfo? {
        return currentBlocksDiscrepancies
                .takeIf {
                    !it.isNullOrEmpty()
                }
                ?.run {
                    searchBlock(currentBlocksDiscrepancies.last().blockDiscrepancies.blockNumber)
                }
    }

    fun addGtin(gtinCode: String) {
        var index = -1
        val currentGtinIndecis = currentGtin.indices
        for (i in currentGtinIndecis) {
            if (gtinCode == currentGtin[i]) {
                index = i
                break
            }
        }

        if (index == -1) {
            currentGtin.add(gtinCode)
            addTypeLastStampScanned(TypeLastStampScanned.GTIN)
        }
    }

    fun getLastScannedGtin(): String? {
        return currentGtin
                .takeIf {
                    !it.isNullOrEmpty()
                }
                ?.run {
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

    fun getCountProcessedBlockForDiscrepancies(typeDiscrepancies: String): Int {
        return currentBlocksDiscrepancies
                .filter {
                    it.blockDiscrepancies.typeDiscrepancies == typeDiscrepancies
                            && it.isGtinControlPassed
                }.size
    }

    private fun rollbackScannedBlock() {
        if (currentBlocksDiscrepancies.isNotEmpty()) {
            val block = currentBlocksDiscrepancies.last()
            currentBlocksDiscrepancies.remove(block)
        }
    }

    private fun rollbackScannedGtin() {
        if (currentGtin.isNotEmpty()) {
            val gtin = currentGtin.last()
            currentGtin.remove(gtin)
        }
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
            it.blockDiscrepancies.blockNumber == code && it.blockDiscrepancies.isScan
        }
    }

    private fun getCountOfDiscrepanciesOfProduct(typeDiscrepancies: String): Double {
        return taskManager
                .getReceivingTask()
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
        return taskManager
                .getReceivingTask()
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
        return taskManager
                .getReceivingTask()
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
        //удаляем всю информацию по блокам
        taskManager
                .getReceivingTask()
                ?.taskRepository
                ?.getBlocksDiscrepancies()
                ?.deleteBlocksDiscrepanciesForProduct(productInfo)
        //отмечаем все блоки/марки для продукта категорией для брака из параметра GRZ_GRUND_MARK
        blocks
                .filter { block ->
                    block.materialNumber == productInfo.materialNumber
                }
                .map { blockInfo ->
                    addBlockDiscrepancies(
                            blockInfo = blockInfo,
                            typeDiscrepancies = typeDiscrepancies,
                            isScan = false, //передаем false, т.к. эта ф-ция (denialOfFullProductAcceptance) вызывается с экрана Обнаружены расхождения по клику на блок на вкладке Не обработаны
                            isGtinControlPassed = true
                    )
                    apply()
                }

        //удаляем всю информацию по продукту
        taskManager
                .getReceivingTask()
                ?.taskRepository
                ?.getProductsDiscrepancies()
                ?.deleteProductsDiscrepanciesForProduct(productInfo)
        //отмечаем по продукту все кол-во с категорией из параметра GRZ_GRUND_MARK
        addProduct(productInfo.origQuantity, typeDiscrepancies)
    }

    fun refusalToAcceptPartlyByProduct(typeDiscrepancies: String) {
        //https://trello.com/c/vcymT9Kp
        val countProcessedBlocks =
                taskManager
                        .getReceivingTask()
                        ?.taskRepository
                        ?.getBlocksDiscrepancies()
                        ?.findBlocksDiscrepanciesOfProduct(productInfo)
                        ?.filter {
                            it.isScan
                        }
                        ?.size
                        .toString()
        val confirmedByScanning = getCountAttachmentInBlock(countProcessedBlocks)
        val notConfirmedByScanning = productInfo.origQuantity.toDouble() - confirmedByScanning

        //отмечаем все не обработанные блоки/марки для продукта категорией для брака из параметра GRZ_GRUND_MARK
        blocks
                .filter { block ->
                    block.materialNumber == productInfo.materialNumber
                            && currentBlocksDiscrepancies.findLast { it.blockDiscrepancies.blockNumber == block.blockNumber } == null
                }.map { blockInfo ->
                    addBlockDiscrepancies(
                            blockInfo = blockInfo,
                            typeDiscrepancies = typeDiscrepancies,
                            isScan = false, //передаем false, т.к. эта ф-ция (refusalToAcceptPartlyByProduct) вызывается с экрана Обнаружены расхождения по клику на блок на вкладке Не обработаны
                            isGtinControlPassed = true
                    )
                    apply()
                }

        //отмечаем продукт (https://trello.com/c/r4kofgtQ в комментарии смотреть логику)
        taskManager
                .getReceivingTask()
                ?.taskRepository
                ?.getProductsDiscrepancies()
                ?.deleteProductDiscrepancy(productInfo.materialNumber, TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM)
        addProduct(confirmedByScanning.toString(), TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM)
        addProduct(notConfirmedByScanning.toString(), typeDiscrepancies)
    }

    fun delBlockDiscrepancy(typeDiscrepancies: String) {
        currentBlocksDiscrepancies.removeItemFromListWithPredicate {
            it.blockDiscrepancies.typeDiscrepancies == typeDiscrepancies
        }
    }

    fun getCountAttachmentInBlock(count: String?): Double {
        val nestingInOneBlock = productInfo.nestingInOneBlock.toDouble()
        return if (isBlockMode) {
            (count?.toDoubleOrNull() ?: 0.0) * nestingInOneBlock
        } else {
            count?.toDoubleOrNull() ?: 0.0
        }
    }

    fun getCountBlocksByAttachments(count: String?): Double {
        val nestingInOneBlock = productInfo.nestingInOneBlock.toDouble()
        return if (isBlockMode) {
            (count?.toDoubleOrNull() ?: 0.0) / nestingInOneBlock
        } else {
            count?.toDoubleOrNull() ?: 0.0
        }
    }

    fun getIsBlockMode(): Boolean {
        return isBlockMode
    }

    private fun addTypeLastStampScanned(typeLastStampScanned: TypeLastStampScanned) {
        currentScannedTypesStamps.add(typeLastStampScanned)
    }

    fun rollbackTypeLastStampScanned() {
        currentScannedTypesStamps
                .takeIf {
                    it.isNotEmpty()
                }
                ?.run {
                    val stamp = currentScannedTypesStamps.last()
                    if (stamp == TypeLastStampScanned.BLOCK) {
                        rollbackScannedBlock()
                    } else {
                        rollbackScannedGtin()
                    }
                    currentScannedTypesStamps.remove(stamp)
                }
    }

    fun modifications(): Boolean {
        return currentBlocksDiscrepancies !=
                taskManager
                        .getReceivingTask()
                        ?.taskRepository
                        ?.getBlocksDiscrepancies()
                        ?.findBlocksDiscrepanciesOfProduct(productInfo)
    }

    fun clearModifications() {
        currentGtin.clear()
        currentScannedTypesStamps.clear()
        blocks.clear()
        taskManager
                .getReceivingTask()
                ?.getProcessedBlocks()
                ?.mapTo(blocks) {
                    it.copy()
                }
        currentBlocksDiscrepancies.clear()
        taskManager
                .getReceivingTask()
                ?.taskRepository
                ?.getBlocksDiscrepancies()
                ?.findBlocksDiscrepanciesOfProduct(productInfo)
                ?.mapTo(currentBlocksDiscrepancies) {
                    BlocksDiscrepanciesInfo(blockDiscrepancies = it.copy(), isGtinControlPassed = true)
                }
    }

    companion object {
        private const val UNIT_ST = "ST"
        private const val UNIT_P09 = "P09"
    }

}

data class BlocksDiscrepanciesInfo(
        val blockDiscrepancies: TaskBlockDiscrepancies,
        val isGtinControlPassed: Boolean
)