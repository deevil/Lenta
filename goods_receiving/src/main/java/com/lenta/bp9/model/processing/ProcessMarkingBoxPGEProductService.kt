package com.lenta.bp9.model.processing

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
class ProcessMarkingBoxPGEProductService
@Inject constructor() : IProcessMarkingProductService {

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var dataBase: IDataBaseRepo

    private lateinit var productInfo: TaskProductInfo
    private val boxes: ArrayList<TaskBoxInfo> = ArrayList()
    private val currentBoxDiscrepancies: ArrayList<TaskBoxDiscrepancies> = ArrayList()
    private val stamps: ArrayList<TaskExciseStampInfo> = ArrayList()
    private val currentStampDiscrepancies: ArrayList<TaskExciseStampDiscrepancies> = ArrayList()
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
                            && getMarkingGoodsRegime(taskManager, inputProductInfo) == MarkingGoodsRegime.UomSTWithBoxesPGE
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

                    stamps.clear()
                    taskRepository
                            ?.getExciseStamps()
                            ?.findExciseStampsOfProduct(productInfo)
                            ?.mapTo(stamps) { it.copy() }

                    currentStampDiscrepancies.clear()
                    taskRepository
                            ?.run {
                                getExciseStampsDiscrepancies()
                                        .findExciseStampsDiscrepanciesOfProduct(productInfo)
                                        .mapTo(currentStampDiscrepancies) {
                                            it.copy()
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

                    var countNotScannedStamps = DEFAULT_DOUBLE_VALUE
                    processedBox
                            .forEach { boxScanned ->
                                countNotScannedStamps +=
                                        repository.run {
                                            getExciseStampsDiscrepancies()
                                                    .findExciseStampsDiscrepanciesOfProduct(productInfo)
                                                    .filter { stampsDiscrepancies ->
                                                        !stampsDiscrepancies.isScan
                                                                && stampsDiscrepancies.boxNumber == boxScanned.boxNumber
                                                    }
                                                    .size
                                                    .toDouble()
                                        }
                            }

                    val countScannedStamps =
                            repository.run {
                                getExciseStampsDiscrepancies()
                                        .findExciseStampsDiscrepanciesOfProduct(productInfo)
                                        .filter { it.isScan }
                                        .size
                                        .toDouble()
                            }

                    return countNotScannedStamps + countScannedStamps
                }
                ?: DEFAULT_DOUBLE_VALUE
    }

    override fun getCountBlocksUnderload(paramGrzGrundMarkCode: String): Double {
        return taskManager
                .getReceivingTask()
                ?.run {
                    taskRepository
                            .getProductsDiscrepancies()
                            .findProductDiscrepanciesOfProduct(productInfo)
                            .findLast { it.typeDiscrepancies == paramGrzGrundMarkCode }
                            ?.numberDiscrepancies
                            ?.toDouble()
                }
                ?: DEFAULT_DOUBLE_VALUE

    }

    override fun denialOfFullProductAcceptance(typeDiscrepancies: String) {
        //https://trello.com/c/vcymT9Kp
        //удаляем всю информацию по коробкам
        taskRepository
                ?.getBoxesDiscrepancies()
                ?.deleteBoxesDiscrepanciesForProduct(productInfo)

        //удаляем всю информацию по блокам
        taskRepository
                ?.getExciseStampsDiscrepancies()
                ?.deleteExciseStampsDiscrepanciesForProduct(productInfo)

        //отмечаем все коробки и блоки для продукта категорией для брака из параметра GRZ_GRUND_MARK
        boxes.forEach {
            addBoxDiscrepancy(
                    boxNumber = it.boxNumber,
                    typeDiscrepancies = typeDiscrepancies,
                    isScan = false,
                    isDenialOfFullProductAcceptance = true
            )
        }

        filterAndUpdateStampDiscrepancies()

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

        filterAndUpdateStampDiscrepancies()

        val notConfirmedByScanning = productInfo.origQuantity.toDouble() - getCountAcceptOfProduct() - getCountRefusalOfProduct()
        addProduct(notConfirmedByScanning.toString(), typeDiscrepancies)
    }

    fun filterAndUpdateStampDiscrepancies() {
        currentStampDiscrepancies
                .takeIf { it.isNotEmpty() }
                ?.filter { currentStampDiscrepancies -> currentStampDiscrepancies.isScan }
                ?.forEach { stamp ->
                    taskRepository
                            ?.getExciseStampsDiscrepancies()
                            ?.changeExciseStampDiscrepancy(stamp)

                }

        currentBoxDiscrepancies
                .takeIf { it.isNotEmpty() }
                ?.forEach { box ->
                    taskRepository
                            ?.getBoxesDiscrepancies()
                            ?.changeBoxDiscrepancy(box)
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
                    .changeProductDiscrepancy(foundDiscrepancy)
        }
    }

    fun addStampDiscrepancies(stampInfo: TaskExciseStampInfo, isScan: Boolean) {
        val boxNumber = stamps
                .findLast { it.boxNumber == stampInfo.boxNumber }
                ?.boxNumber
                .orEmpty()

        var foundStampDiscrepancy =
                currentStampDiscrepancies
                        .findLast { it.materialNumber == stampInfo.materialNumber }

        foundStampDiscrepancy = foundStampDiscrepancy
                ?.let {
                    it.copy()
                }
                ?: TaskExciseStampDiscrepancies(
                        materialNumber = stampInfo.materialNumber.orEmpty(),
                        code = stampInfo.code.orEmpty(),
                        processingUnitNumber = stampInfo.processingUnitNumber.orEmpty(),
                        typeDiscrepancies = "",
                        isScan = isScan,
                        boxNumber = boxNumber,
                        packNumber = "",
                        isMSC = false,
                        organizationCodeEGAIS = "",
                        bottlingDate = "",
                        isUnknown = false
                )


        currentStampDiscrepancies.removeItemFromListWithPredicate { stamp ->
            stamp.materialNumber == stampInfo.materialNumber
        }

        currentStampDiscrepancies.add(foundStampDiscrepancy)

        addTypeLastStampScanned(TypeLastStampScanned.BLOCK)
    }

    fun addBoxDiscrepancy(boxNumber: String, typeDiscrepancies: String, isScan: Boolean, isDenialOfFullProductAcceptance: Boolean): Int {
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

        //сохраняем все необработанные блоки для коробки без isScan
        var countAddStamps = DEFAULT_INT_VALUE
        val stampsFromBox = stamps.filter { it.boxNumber == boxNumber }
        stampsFromBox
                .filter { stamp ->
                    val lastStamp = currentStampDiscrepancies.findLast { it.materialNumber == stamp.materialNumber }
                    lastStamp == null
                }
                .forEach { stampInfo ->
                    addStampDiscrepancies(
                            stampInfo = stampInfo,
                            isScan = false
                    )
                    countAddStamps += 1
                }

        addTypeLastStampScanned(TypeLastStampScanned.BOX)

        return countAddStamps
    }

    fun addAllUntreatedStampsAsDefect() {
        //отмечаем все не обработанные блоки/марки для продукта категорией выбранной категорией для брака
        stamps.filter { stamp ->
            currentStampDiscrepancies.findLast { it.materialNumber == stamp.materialNumber } == null
        }.forEach { stampInfo ->
            addStampDiscrepancies(
                    stampInfo = stampInfo,
                    isScan = false  //передаем false, т.к. эта ф-ция вызывается при сохранении всех необработанных блоков в брак без сканирования
            )
        }
    }

    fun getTotalScannedBoxes(): Int {
        return currentStampDiscrepancies.size
    }

    fun getCountProcessedStampForDiscrepancies(typeDiscrepancies: String): Int {
        return currentStampDiscrepancies.count { it.typeDiscrepancies == typeDiscrepancies }
    }


    fun isOverLimit(count: Double): Boolean {
        return productInfo.orderQuantity.toDouble() / productInfo.quantityInvest.toDouble() < (getCountAcceptOfProduct() + getCountRefusalOfProduct() + count)
    }

    fun searchStamp(stampNumber: String): TaskExciseStampInfo? {
        return stamps.findLast {
            it.materialNumber == stampNumber
        }
    }


    fun searchBoxDiscrepancies(blockNumber: String): TaskBoxDiscrepancies? {
        return currentBoxDiscrepancies.findLast {
            it.boxNumber == blockNumber
        }
    }

    fun stampIsAlreadyProcessed(code: String): Boolean {
        return currentStampDiscrepancies.any {
            it.materialNumber == code && it.isScan
        }
    }

    fun boxIsAlreadyProcessed(code: String): Boolean {
        return currentBoxDiscrepancies.any {
            it.boxNumber == code && it.isScan
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
                ?: DEFAULT_DOUBLE_VALUE
    }

    fun getCountAcceptOfProduct(): Double {
        return taskRepository
                ?.run {
                    getProductsDiscrepancies()
                            .findProductDiscrepanciesOfProduct(productInfo)
                            .filter { productDiscrepancies -> productDiscrepancies.typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM }
                            .sumByDouble { it.numberDiscrepancies.toDouble() }
                }
                ?: DEFAULT_DOUBLE_VALUE
    }

    fun getCountRefusalOfProduct(): Double {
        return taskRepository
                ?.run {
                    getProductsDiscrepancies()
                            .findProductDiscrepanciesOfProduct(productInfo)
                            .filter { productDiscrepancies -> productDiscrepancies.typeDiscrepancies != TYPE_DISCREPANCIES_QUALITY_NORM }
                            .sumByDouble { it.numberDiscrepancies.toDouble() }
                }
                ?: DEFAULT_DOUBLE_VALUE
    }

    private fun addTypeLastStampScanned(typeLastStampScanned: TypeLastStampScanned) {
        currentScannedTypesStamps.add(typeLastStampScanned)
    }

    fun modifications(): Boolean {
        val stampsDiscrepanciesOfProduct =
                taskRepository
                        ?.getExciseStampsDiscrepancies()
                        ?.findExciseStampsDiscrepanciesOfProduct(productInfo)

        return currentStampDiscrepancies != stampsDiscrepanciesOfProduct
    }

    fun clearModifications() {
        currentGtin.clear()
        currentScannedTypesStamps.clear()
        boxes.clear()
        currentBoxDiscrepancies.clear()
        stamps.clear()
        currentStampDiscrepancies.clear()

        receivingTask
                ?.getProcessedExciseStamps()
                ?.mapTo(stamps) { it.copy() }

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
                        getExciseStampsDiscrepancies()
                                .findExciseStampsDiscrepanciesOfProduct(productInfo)
                                .mapTo(currentStampDiscrepancies) { it.copy() }
                    }
        }
    }

    fun apply() {
        currentStampDiscrepancies.forEach {
            taskRepository
                    ?.getExciseStampsDiscrepancies()
                    ?.changeExciseStampDiscrepancy(it)
        }

        currentBoxDiscrepancies.forEach {
            taskRepository
                    ?.getBoxesDiscrepancies()
                    ?.changeBoxDiscrepancy(it)
        }
    }

    companion object {
        private const val DEFAULT_DOUBLE_VALUE = 0.0
        private const val DEFAULT_INT_VALUE = 0
    }
}

