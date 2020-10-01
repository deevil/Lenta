package com.lenta.bp9.model.processing

import com.lenta.bp9.features.goods_information.marking.MarkingBlocksDiscrepanciesInfo
import com.lenta.bp9.features.goods_information.marking.TypeLastStampScanned
import com.lenta.bp9.model.repositories.ITaskRepository
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.di.AppScope
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.utilities.extentions.removeItemFromListWithPredicate
import javax.inject.Inject

@AppScope
class ProcessMarkingBoxProductService
@Inject constructor() : IProcessMarkingProductService {

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var dataBase: IDataBaseRepo

    private lateinit var productInfo: TaskProductInfo
    private val boxes: ArrayList<TaskBoxInfo> = ArrayList()
    private val currentBoxDiscrepancies: ArrayList<TaskBoxDiscrepancies> = ArrayList()
    private val blocks: ArrayList<TaskBlockInfo> = ArrayList()
    private val currentBlocksDiscrepancies: ArrayList<MarkingBlocksDiscrepanciesInfo> = ArrayList()
    private val currentGtin: ArrayList<String> = ArrayList()
    private val currentScannedTypesStamps: ArrayList<TypeLastStampScanned> = ArrayList()
    private var taskRepository: ITaskRepository? = null
    private var receivingTask: ReceivingTask? = null

    fun initProduct(inputProductInfo: TaskProductInfo) {
        this.productInfo = inputProductInfo.copy()
    }

    override fun newProcessMarkingProductService(inputProductInfo: TaskProductInfo): IProcessMarkingProductService? {
        return this
                .takeIf {
                    inputProductInfo.type == ProductType.General
                            && getMarkingGoodsRegime(taskManager,inputProductInfo) == MarkingGoodsRegime.UomStWithBoxes
                }
                ?.apply {
                    this.productInfo = inputProductInfo.copy()
                    receivingTask = taskManager.getReceivingTask()
                    taskRepository = receivingTask?.taskRepository
                    currentGtin.clear()
                    currentScannedTypesStamps.clear()

                    boxes.clear()
                    taskRepository?.let { repository ->
                        repository.getBoxesRepository()
                                .findBoxesOfProduct(productInfo)
                                ?.mapTo(boxes) { it.copy() }
                    }

                    currentBoxDiscrepancies.clear()
                    taskRepository
                            ?.apply {
                                getBoxesDiscrepancies()
                                        .findBoxesDiscrepanciesOfProduct(productInfo)
                                        .mapTo(currentBoxDiscrepancies) { it.copy() }
                            }

                    blocks.clear()
                    taskRepository
                            ?.getBlocks()
                            ?.findBlocksOfProduct(productInfo)
                            ?.mapTo(blocks) { it.copy() }

                    currentBlocksDiscrepancies.clear()
                    taskRepository
                            ?.run {
                                getBlocksDiscrepancies()
                                        .findBlocksDiscrepanciesOfProduct(productInfo)
                                        .mapTo(currentBlocksDiscrepancies) {
                                            MarkingBlocksDiscrepanciesInfo(it.copy(), true)
                                        }
                            }
                }
    }

    override fun getConfirmedByScanning(): Double {
        return taskRepository
                ?.let { repository ->
                    val processedBox =
                            repository.run {
                                getBoxesDiscrepancies()
                                        .findBoxesDiscrepanciesOfProduct(productInfo)
                                        .filter { box -> box.isScan }
                            }

                    var countNotScannedBlocks = 0.0
                    processedBox
                            .forEach { boxScanned ->
                                countNotScannedBlocks +=
                                        repository.run {
                                            getBlocksDiscrepancies()
                                                    .findBlocksDiscrepanciesOfProduct(productInfo)
                                                    .filter { blocksDiscrepancies ->
                                                        !blocksDiscrepancies.isScan
                                                                && blocksDiscrepancies.boxNumber == boxScanned.boxNumber
                                                    }
                                                    .size
                                                    .toDouble()
                                        }
                            }

                    val countScannedBlocks =
                            repository.run {
                                getBlocksDiscrepancies()
                                        .findBlocksDiscrepanciesOfProduct(productInfo)
                                        .filter { it.isScan }
                                        .size
                                        .toDouble()
                            }

                    val totalCountProcessedBlocks = countNotScannedBlocks + countScannedBlocks

                    val nestingInOneBlock = productInfo.nestingInOneBlock.toDouble()

                    return totalCountProcessedBlocks * nestingInOneBlock
                }
                ?: 0.0
    }

    override fun getCountBlocksUnderload(paramGrzGrundMarkCode: String): Double {
        val countUnderload =
                taskManager
                        .getReceivingTask()
                        ?.run {
                            taskRepository
                                    .getProductsDiscrepancies()
                                    .findProductDiscrepanciesOfProduct(productInfo)
                                    .findLast { it.typeDiscrepancies == paramGrzGrundMarkCode }
                                    ?.numberDiscrepancies
                                    ?.toDouble()
                        }
                        ?: 0.0

        val nestingInOneBlock = productInfo.nestingInOneBlock.toDouble()

        return countUnderload / nestingInOneBlock
    }

    override fun denialOfFullProductAcceptance(typeDiscrepancies: String) {
        //https://trello.com/c/vcymT9Kp
        //удаляем всю информацию по коробкам
        taskRepository
                ?.getBoxesDiscrepancies()
                ?.deleteBoxesDiscrepanciesForProduct(productInfo)

        //удаляем всю информацию по блокам
        taskRepository
                ?.getBlocksDiscrepancies()
                ?.deleteBlocksDiscrepanciesForProduct(productInfo)

        //отмечаем все коробки и блоки для продукта категорией для брака из параметра GRZ_GRUND_MARK
        boxes.forEach {
            addBoxDiscrepancy(
                    boxNumber = it.boxNumber,
                    typeDiscrepancies = typeDiscrepancies,
                    isScan = false,
                    isDenialOfFullProductAcceptance = true
            )
        }

        apply()

        //удаляем всю информацию по продукту
        taskRepository
                ?.getProductsDiscrepancies()
                ?.deleteProductsDiscrepanciesForProduct(productInfo)

        //отмечаем по продукту все кол-во с категорией из параметра GRZ_GRUND_MARK
        addProduct(productInfo.origQuantity, typeDiscrepancies)
    }

    override fun refusalToAcceptPartlyByProduct(typeDiscrepancies: String) {
        //https://trello.com/c/vcymT9Kp
        //отмечаем все необработанные коробки и блоки для продукта категорией для брака из параметра GRZ_GRUND_MARK
        boxes.filter { boxInfo ->
            val lastBox = currentBoxDiscrepancies.findLast { it.boxNumber == boxInfo.boxNumber }
            lastBox == null
        }.forEach {
            addBoxDiscrepancy(
                    boxNumber = it.boxNumber,
                    typeDiscrepancies = typeDiscrepancies,
                    isScan = false,
                    isDenialOfFullProductAcceptance = false
            )
        }

        apply()

        val notConfirmedByScanning = productInfo.origQuantity.toDouble() - getCountAcceptOfProduct() - getCountRefusalOfProduct()
        addProduct(notConfirmedByScanning.toString(), typeDiscrepancies)
    }

    fun apply() {
        currentBlocksDiscrepancies
                .takeIf { it.isNotEmpty() }
                ?.apply {
                    filter { blockDiscrepanciesInfo -> blockDiscrepanciesInfo.isGtinControlPassed }
                            .forEach { block ->
                                taskRepository
                                        ?.getBlocksDiscrepancies()
                                        ?.changeBlockDiscrepancy(block.blockDiscrepancies)
                            }
                }

        currentBoxDiscrepancies
                .takeIf { it.isNotEmpty() }
                ?.apply {
                    forEach { box ->
                        taskRepository
                                ?.getBoxesDiscrepancies()
                                ?.changeBoxDiscrepancy(box)
                    }
                }
    }

    fun addProduct(count: String, typeDiscrepancies: String) {
        taskRepository?.let { repository ->
            val countAdd =
                    if (typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM) {
                        getCountAcceptOfProduct() + count.toDouble()
                    } else {
                        getCountOfDiscrepanciesOfProduct(typeDiscrepancies) + count.toDouble()
                    }

            //добавляем кол-во по расхождению для продукта
            var foundDiscrepancy =
                    repository
                            .run {
                                getProductsDiscrepancies()
                                        .findProductDiscrepanciesOfProduct(productInfo)
                                        .findLast { it.typeDiscrepancies == typeDiscrepancies }
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

            repository
                    .getProductsDiscrepancies()
                    .changeProductDiscrepancyOfProcessingUnit(foundDiscrepancy)
        }
    }

    fun addBlockDiscrepancies(blockInfo: TaskBlockInfo, typeDiscrepancies: String, isScan: Boolean, isGtinControlPassed: Boolean) {
        val boxNumber = blocks
                .findLast { it.blockNumber == blockInfo.blockNumber }
                ?.boxNumber
                .orEmpty()

        var foundBlockDiscrepancy =
                currentBlocksDiscrepancies
                        .findLast { it.blockDiscrepancies.blockNumber == blockInfo.blockNumber }

        foundBlockDiscrepancy =
                foundBlockDiscrepancy
                        ?.let {
                            MarkingBlocksDiscrepanciesInfo(
                                    blockDiscrepancies = it.blockDiscrepancies.copy(typeDiscrepancies = typeDiscrepancies),
                                    isGtinControlPassed = it.isGtinControlPassed
                            )
                        }
                        ?: MarkingBlocksDiscrepanciesInfo(
                                blockDiscrepancies = TaskBlockDiscrepancies(
                                        processingUnitNumber = blockInfo.processingUnitNumber,
                                        materialNumber = blockInfo.materialNumber,
                                        blockNumber = blockInfo.blockNumber,
                                        boxNumber = boxNumber,
                                        typeDiscrepancies = typeDiscrepancies,
                                        isScan = isScan,
                                        isMsc = false,
                                        isUnknown = false,
                                        isGrayZone = false
                                ),
                                isGtinControlPassed = isGtinControlPassed
                        )

        currentBlocksDiscrepancies.removeItemFromListWithPredicate { block ->
            block.blockDiscrepancies.blockNumber == blockInfo.blockNumber
        }

        currentBlocksDiscrepancies.add(foundBlockDiscrepancy)

        addTypeLastStampScanned(TypeLastStampScanned.BLOCK)
    }

    fun addBoxDiscrepancy(boxNumber: String, typeDiscrepancies: String, isScan: Boolean, isDenialOfFullProductAcceptance: Boolean) : Int {
        val box = boxes.findLast { it.boxNumber == boxNumber }
        var foundBoxDiscrepancy =
                currentBoxDiscrepancies
                        .findLast { it.boxNumber == boxNumber }

        foundBoxDiscrepancy = foundBoxDiscrepancy?.copy(typeDiscrepancies = typeDiscrepancies)
                ?: TaskBoxDiscrepancies(
                        processingUnitNumber = box?.processingUnitNumber.orEmpty(),
                        materialNumber = box?.materialNumber.orEmpty(),
                        boxNumber = boxNumber,
                        typeDiscrepancies = typeDiscrepancies,
                        isScan = isScan
                )

        currentBoxDiscrepancies.removeItemFromListWithPredicate { unitInfo ->
            unitInfo.boxNumber == boxNumber
        }

        currentBoxDiscrepancies.add(foundBoxDiscrepancy)

        //сохраняем все необработанные блоки для коробки без isScan, но с isGtinControlPassed
        var countAddBlocks = 0
        val blocksFromBox = blocks.filter { it.boxNumber == boxNumber }
        blocksFromBox
                .filter { block ->
                    val lastBlock = currentBlocksDiscrepancies.findLast { it.blockDiscrepancies.blockNumber == block.blockNumber }
                    lastBlock == null || isDenialOfFullProductAcceptance
                }
                .forEach { blockInfo ->
                    addBlockDiscrepancies(
                            blockInfo = blockInfo,
                            typeDiscrepancies = typeDiscrepancies,
                            isScan = false,
                            isGtinControlPassed = true
                    )
                    countAddBlocks += 1
                }

        addTypeLastStampScanned(TypeLastStampScanned.BOX)

        return countAddBlocks
    }

    fun markPassageControlBlock(blockNumber: String) {
        val blockDiscrepancies =
                currentBlocksDiscrepancies
                        .findLast { it.blockDiscrepancies.blockNumber == blockNumber }
                        ?.blockDiscrepancies

        blockDiscrepancies?.let {
            for (i in currentBlocksDiscrepancies.indices) {
                if (currentBlocksDiscrepancies[i].blockDiscrepancies.blockNumber == blockNumber) {
                    currentBlocksDiscrepancies[i] = MarkingBlocksDiscrepanciesInfo(it, true)
                    break
                }
            }
        }
    }

    fun addAllUntreatedBlocksAsDefect(typeDiscrepancies: String) {
        //отмечаем все не обработанные блоки/марки для продукта категорией выбранной категорией для брака
        blocks.filter { block ->
            currentBlocksDiscrepancies.findLast { it.blockDiscrepancies.blockNumber == block.blockNumber } == null
        }.forEach { blockInfo ->
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
                .takeIf { !it.isNullOrEmpty() }
                ?.run { searchBlock(currentBlocksDiscrepancies.last().blockDiscrepancies.blockNumber) }
    }

    fun addGtin(gtinCode: String) {
        var index = currentGtin.indexOfFirst { it == gtinCode }
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
                .takeIf { !it.isNullOrEmpty() }
                ?.run { currentGtin.last() }
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
        return currentBlocksDiscrepancies.count { it.blockDiscrepancies.typeDiscrepancies == typeDiscrepancies && it.isGtinControlPassed }
    }

    fun checkBlocksCategoriesDifferentCurrent(boxNumber: String, typeDiscrepancies: String): Boolean {
        return currentBlocksDiscrepancies.any {
            it.blockDiscrepancies.typeDiscrepancies != typeDiscrepancies
                    && it.blockDiscrepancies.boxNumber == boxNumber
        }
    }

    private fun rollbackScannedBlock() {
        currentBlocksDiscrepancies
                .takeIf { it.isNotEmpty() }
                ?.apply {
                    val block = this.last()
                    this.remove(block)
                }
    }

    private fun rollbackScannedGtin() {
        currentGtin
                .takeIf { it.isNotEmpty() }
                ?.apply {
                    val gtin = this.last()
                    this.remove(gtin)
                }
    }

    private fun rollbackScannedBox() : Int {
        var boxNumber = ""
        currentBoxDiscrepancies
                .takeIf { it.isNotEmpty() }
                ?.apply {
                    val box = this.last()
                    boxNumber = box.boxNumber
                    this.remove(box)
                }

        val countDelBlocksForBox =
                currentBlocksDiscrepancies.count { it.blockDiscrepancies.boxNumber == boxNumber && !it.blockDiscrepancies.isScan }

        //удаляем блоки, которые были добавлены при скане коробки, т.е. без признака isScan
        currentBlocksDiscrepancies
                .removeItemFromListWithPredicate {
                    it.blockDiscrepancies.boxNumber == boxNumber && !it.blockDiscrepancies.isScan
                }

        return countDelBlocksForBox
    }

    fun isOverLimit(count: Double): Boolean {
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
        return taskRepository
                ?.run {
                    getProductsDiscrepancies()
                            .findProductDiscrepanciesOfProduct(productInfo)
                            .filter { productDiscrepancies -> productDiscrepancies.typeDiscrepancies == typeDiscrepancies }
                            .sumByDouble { it.numberDiscrepancies.toDouble() }
                }
                ?: 0.0
    }

    fun getCountAcceptOfProduct(): Double {
        return taskRepository
                ?.run {
                    getProductsDiscrepancies()
                            .findProductDiscrepanciesOfProduct(productInfo)
                            .filter { productDiscrepancies -> productDiscrepancies.typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM }
                            .sumByDouble { it.numberDiscrepancies.toDouble() }
                }
                ?: 0.0
    }

    fun getCountRefusalOfProduct(): Double {
        return taskRepository
                ?.run {
                    getProductsDiscrepancies()
                            .findProductDiscrepanciesOfProduct(productInfo)
                            .filter { productDiscrepancies -> productDiscrepancies.typeDiscrepancies != TYPE_DISCREPANCIES_QUALITY_NORM }
                            .sumByDouble { it.numberDiscrepancies.toDouble() }
                }
                ?: 0.0
    }

    fun delBoxAndBlockDiscrepancy(typeDiscrepancies: String) {
        currentBoxDiscrepancies.removeItemFromListWithPredicate {
            it.typeDiscrepancies == typeDiscrepancies
        }

        currentBlocksDiscrepancies.removeItemFromListWithPredicate {
            it.blockDiscrepancies.typeDiscrepancies == typeDiscrepancies
        }
    }

    private fun addTypeLastStampScanned(typeLastStampScanned: TypeLastStampScanned) {
        currentScannedTypesStamps.add(typeLastStampScanned)
    }

    fun rollbackTypeLastStampScanned() : Int {
        var countDelBlocksForBox = 0
        currentScannedTypesStamps
                .takeIf { it.isNotEmpty() }
                ?.apply {
                    val stamp = this.last()
                    when (stamp) {
                        TypeLastStampScanned.BLOCK -> rollbackScannedBlock()
                        TypeLastStampScanned.GTIN -> rollbackScannedGtin()
                        TypeLastStampScanned.BOX -> countDelBlocksForBox = rollbackScannedBox()
                        else -> return@apply
                    }
                    this.remove(stamp)
                }

        return countDelBlocksForBox
    }

    fun getLastScannedTypesStamps() : TypeLastStampScanned {
        return currentScannedTypesStamps.last()
    }

    fun modifications(): Boolean {
        val blocksDiscrepanciesOfProduct =
                taskRepository
                        ?.getBlocksDiscrepancies()
                        ?.findBlocksDiscrepanciesOfProduct(productInfo)

        return currentBlocksDiscrepancies != blocksDiscrepanciesOfProduct
    }

    fun clearModifications() {
        currentGtin.clear()
        currentScannedTypesStamps.clear()
        boxes.clear()
        currentBoxDiscrepancies.clear()
        blocks.clear()
        currentBlocksDiscrepancies.clear()

        receivingTask
                ?.getProcessedBlocks()
                ?.mapTo(blocks) { it.copy() }

        taskRepository?.let { repository ->
            repository
                    .getBoxesRepository()
                    .findBoxesOfProduct(productInfo)
                    ?.mapTo(boxes) { it.copy() }


            repository
                    .apply {
                        getBoxesDiscrepancies()
                                .findBoxesDiscrepanciesOfProduct(productInfo)
                                .mapTo(currentBoxDiscrepancies) { it.copy() }
                    }

            repository
                    .apply {
                        getBlocksDiscrepancies()
                                .findBlocksDiscrepanciesOfProduct(productInfo)
                                .mapTo(currentBlocksDiscrepancies) {
                                    MarkingBlocksDiscrepanciesInfo(blockDiscrepancies = it.copy(), isGtinControlPassed = true)
                                }
                    }
        }
    }

}