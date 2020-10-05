package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.shared.di.AppScope
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.utilities.extentions.removeItemFromListWithPredicate
import javax.inject.Inject

@AppScope
class ProcessExciseAlcoBoxAccPGEService
@Inject constructor() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    private lateinit var productInfo: TaskProductInfo
    private val boxes: ArrayList<TaskBoxInfo> = ArrayList()
    private val currentBoxDiscrepancies: ArrayList<TaskBoxDiscrepancies> = ArrayList()
    private val exciseStamps: ArrayList<TaskExciseStampInfo> = ArrayList()
    private val currentExciseStampsDiscrepancies: ArrayList<TaskExciseStampDiscrepancies> = ArrayList()
    private var initialCount: Double = 0.0
    private var countAcceptRefusal: Double = 0.0

    fun newProcessExciseAlcoBoxPGEService(productInfo: TaskProductInfo): ProcessExciseAlcoBoxAccPGEService? {
        return if (productInfo.type == ProductType.ExciseAlcohol && productInfo.isBoxFl) { //алкоголь, коробочный учет https://trello.com/c/TzUSGIH7
            this.productInfo = productInfo.copy()
            boxes.clear()
            taskManager.getReceivingTask()?.taskRepository?.getBoxesRepository()?.getTaskBoxes()?.map {
                boxes.add(it.copy())
            }
            currentBoxDiscrepancies.clear()
            taskManager.getReceivingTask()?.taskRepository?.getBoxesDiscrepancies()?.findBoxesDiscrepanciesOfProduct(productInfo)?.map {
                currentBoxDiscrepancies.add(it.copy())
            }
            exciseStamps.clear()
            taskManager.getReceivingTask()?.taskRepository?.getExciseStamps()?.getExciseStamps()?.map {
                exciseStamps.add(it.copy())
            }
            currentExciseStampsDiscrepancies.clear()
            taskManager.getReceivingTask()?.taskRepository?.getExciseStampsDiscrepancies()?.findExciseStampsDiscrepanciesOfProduct(productInfo)?.map {
                currentExciseStampsDiscrepancies.add(it.copy())
            }
            initialCount = 0.0
            countAcceptRefusal = 0.0
            this
        } else null
    }

    fun applyBoxCard() {
        if (currentBoxDiscrepancies.isNotEmpty()) {
            currentBoxDiscrepancies.map {
                taskManager
                        .getReceivingTask()
                        ?.taskRepository
                        ?.getBoxesDiscrepancies()
                        ?.changeBoxDiscrepancy(it)
            }
        }

        if (currentExciseStampsDiscrepancies.isNotEmpty()) {
            currentExciseStampsDiscrepancies.map {
                taskManager.getReceivingTask()
                        ?.taskRepository
                        ?.getExciseStampsDiscrepancies()
                        ?.changeExciseStampDiscrepancy(it)
            }
        }
    }

    fun addProduct(count: String, typeDiscrepancies: String) {
        val countAdd = getCountOfDiscrepanciesOfProduct(typeDiscrepancies) + count.toDouble()

        //добавляем кол-во по расхождению для продукта
        var foundDiscrepancy =
                taskManager
                        .getReceivingTask()
                        ?.taskRepository
                        ?.getProductsDiscrepancies()
                        ?.findProductDiscrepanciesOfProduct(productInfo)
                        ?.findLast { it.typeDiscrepancies == typeDiscrepancies }

        foundDiscrepancy = foundDiscrepancy?.copy(numberDiscrepancies = countAdd.toString(), processingUnitNumber = productInfo.processingUnit)
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

    fun addExciseStampDiscrepancy(exciseStamp: TaskExciseStampInfo, typeDiscrepancies: String, isScan: Boolean) {
        var foundExciseStampDiscrepancy = currentExciseStampsDiscrepancies.findLast {
            it.code == exciseStamp.code
        }

        foundExciseStampDiscrepancy = foundExciseStampDiscrepancy?.copy(typeDiscrepancies = typeDiscrepancies, isScan = isScan)
                ?: TaskExciseStampDiscrepancies(
                        materialNumber = exciseStamp.materialNumber.orEmpty(),
                        code = exciseStamp.code.orEmpty(),
                        processingUnitNumber = exciseStamp.processingUnitNumber.orEmpty(),
                        typeDiscrepancies = typeDiscrepancies,
                        isScan = isScan,
                        boxNumber = exciseStamp.boxNumber.orEmpty(),
                        packNumber = "",
                        isMSC = false,
                        organizationCodeEGAIS = exciseStamp.organizationCodeEGAIS.orEmpty(),
                        bottlingDate = exciseStamp.bottlingDate.orEmpty(),
                        isUnknown = false
                )

        currentExciseStampsDiscrepancies.removeItemFromListWithPredicate {stamp ->
            stamp.code == exciseStamp.code
        }

        currentExciseStampsDiscrepancies.add(foundExciseStampDiscrepancy)
    }

    //https://trello.com/c/lWYJ43Pe (Марка-излишек внутри коробки) карточка об этом условии if (isExciseStampSurplus.value == true) "2"
    fun addExciseStampSurplus(exciseStampCode: String, boxNumber: String, organizationCodeEGAIS: String, bottlingDate: String, isScan: Boolean) {
        var foundExciseStampDiscrepancy = currentExciseStampsDiscrepancies.findLast {
            it.code == exciseStampCode
        }

        foundExciseStampDiscrepancy = foundExciseStampDiscrepancy?.copy(
                typeDiscrepancies = TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS,
                isScan = isScan
        )
                ?: TaskExciseStampDiscrepancies(
                        materialNumber = productInfo.materialNumber,
                        code = exciseStampCode,
                        processingUnitNumber = productInfo.processingUnit,
                        typeDiscrepancies = TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS,
                        isScan = isScan,
                        boxNumber = boxNumber,
                        packNumber = "",
                        isMSC = false,
                        organizationCodeEGAIS = organizationCodeEGAIS,
                        bottlingDate = bottlingDate,
                        isUnknown = false
                )

        currentExciseStampsDiscrepancies.map { it }.filter { unitInfo ->
            if (unitInfo.code == exciseStampCode) {
                currentExciseStampsDiscrepancies.remove(unitInfo)
                return@filter true
            }
            return@filter false
        }

        currentExciseStampsDiscrepancies.add(foundExciseStampDiscrepancy)
    }

    fun addBoxDiscrepancy(boxNumber: String, typeDiscrepancies: String, isScan: Boolean) {
        val box = boxes.findLast {
            it.boxNumber == boxNumber
        }

        var foundBoxDiscrepancy = currentBoxDiscrepancies.findLast {
            it.boxNumber == boxNumber
        }

        foundBoxDiscrepancy = foundBoxDiscrepancy?.copy(typeDiscrepancies = typeDiscrepancies, isScan = isScan)
                ?: TaskBoxDiscrepancies(
                        processingUnitNumber = box?.processingUnitNumber ?: "",
                        materialNumber = box?.materialNumber ?: "",
                        boxNumber = boxNumber,
                        typeDiscrepancies = typeDiscrepancies,
                        isScan = isScan
                )

        currentBoxDiscrepancies.map { it }.filter { unitInfo ->
            if (unitInfo.boxNumber == boxNumber) {
                currentBoxDiscrepancies.remove(unitInfo)
                return@filter true
            }
            return@filter false
        }

        currentBoxDiscrepancies.add(foundBoxDiscrepancy)
    }

    fun addAllAsSurplusForBox(count: String, boxNumber: String, typeDiscrepancies: String, isScan: Boolean) { //https://trello.com/c/TzUSGIH7
        addProduct(count, typeDiscrepancies)
        addBoxDiscrepancy(boxNumber, typeDiscrepancies, isScan)
        //отмечаем все марки из коробки признаком IS_SCAN=false (в карточке трелло не было указано ставить true для этих марок и мы отсканировали коробку, а не марки) и категорией Излишек
        exciseStamps.filter { stamp ->
            stamp.boxNumber == boxNumber
        }.map {
            addExciseStampDiscrepancy(it, typeDiscrepancies, false)
        }

    }

    fun overLimit(count: Double): Boolean {
        return (getCountAcceptOfProduct() + getCountRefusalOfProduct() + count) > (productInfo.orderQuantity.toDouble() + getCountBoxesOfProductWithCategorySurplus())
    }

    private fun getCountBoxesOfProductWithCategorySurplus(): Double {
        return currentBoxDiscrepancies.filter {
            it.typeDiscrepancies == "2"
        }.size.toDouble()
    }

    fun getCountBoxesOfProductForSearchSurplus(count: Double): Double {
        return (getCountAcceptOfProduct() + getCountRefusalOfProduct() + count) - (productInfo.orderQuantity.toDouble() - getCountBoxesOfProductWithCategorySurplus())
    }

    fun searchExciseStamp(code: String): TaskExciseStampInfo? {
        return exciseStamps.findLast {
            it.code == code
        }
    }

    fun exciseStampIsAlreadyProcessed(code: String): Boolean {
        return currentExciseStampsDiscrepancies.any {
            it.code == code && it.isScan
        }
    }

    fun searchBox(boxNumber: String): TaskBoxInfo? {
        return boxes.findLast {
            it.boxNumber == boxNumber
        }
    }

    fun getCountBoxOfProductOfDiscrepancies(boxNumber: String): Int {
        return currentBoxDiscrepancies.filter {
            it.boxNumber == boxNumber
        }.size
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
                            || productDiscrepancies.typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS
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
                    productDiscrepancies.typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_UNDERLOAD
                            || productDiscrepancies.typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_MARRIAGE_SHIPMENT
                            || productDiscrepancies.typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_WAREHOUSE_MARRIAGE
                }?.sumByDouble {
                    it.numberDiscrepancies.toDouble()
                }
                ?: 0.0
    }

    fun getCountExciseStampDiscrepanciesOfBox(boxNumber: String): Int {
        return currentExciseStampsDiscrepancies.filter {
            it.boxNumber == boxNumber && it.isScan
        }.size
    }

    //контроль марки для короба
    fun stampControlOfBox(box: TaskBoxInfo): Boolean {
        val countScannedExciseStampsDiscrepanciesOfBox = currentExciseStampsDiscrepancies.filter {
            it.boxNumber == box.boxNumber && it.isScan
        }.size

        return countScannedExciseStampsDiscrepanciesOfBox >= productInfo.numberStampsControl.toInt()
    }

    // контроль короба
    fun boxControl(box: TaskBoxInfo): Boolean {
        val countProcessedBox = currentBoxDiscrepancies.filter {
            it.boxNumber == box.boxNumber
        }.size
        val countScannedExciseStampOfBox = currentExciseStampsDiscrepancies.filter {
            it.boxNumber == box.boxNumber && it.isScan
        }.size

        return (countProcessedBox >= 1 && countScannedExciseStampOfBox >= 1) || (countScannedExciseStampOfBox >= 2) || (countScannedExciseStampOfBox >= productInfo.numberStampsControl.toInt())
    }

    fun rollbackScannedExciseStamp() {
        val stamp = currentExciseStampsDiscrepancies.last {
            it.isScan
        }
        currentExciseStampsDiscrepancies.remove(stamp)
    }

    fun getLastAddExciseStamp(): TaskExciseStampInfo? {
        return if (currentExciseStampsDiscrepancies.isNotEmpty()) {
            exciseStamps.findLast { stampInfo ->
                stampInfo.code == currentExciseStampsDiscrepancies.last {
                    it.isScan
                }.code
            }
        } else null
    }

    fun getCountUntreatedBoxes(): Int {
        val countTotalBoxes = boxes.filter { it.materialNumber == productInfo.materialNumber }.size
        val countProcessedBoxes = currentBoxDiscrepancies.filter { it.materialNumber == productInfo.materialNumber }.size
        return countTotalBoxes - countProcessedBoxes
    }

    fun modifications(): Boolean {
        return currentExciseStampsDiscrepancies != taskManager.getReceivingTask()?.taskRepository?.getExciseStampsDiscrepancies()?.getExciseStampDiscrepancies() ||
                currentBoxDiscrepancies != taskManager.getReceivingTask()?.taskRepository?.getBoxesDiscrepancies()?.getBoxesDiscrepancies()
    }

    fun boxProcessed(boxNumber: String): Boolean {
        return currentBoxDiscrepancies.any { it.boxNumber == boxNumber }
    }

    fun clearModifications() {
        currentBoxDiscrepancies.clear()
        taskManager.getReceivingTask()?.taskRepository?.getBoxesDiscrepancies()?.findBoxesDiscrepanciesOfProduct(productInfo)?.map {
            currentBoxDiscrepancies.add(it.copy())
        }
        currentExciseStampsDiscrepancies.clear()
        taskManager.getReceivingTask()?.taskRepository?.getExciseStampsDiscrepancies()?.findExciseStampsDiscrepanciesOfProduct(productInfo)?.map {
            currentExciseStampsDiscrepancies.add(it.copy())
        }
    }

    fun defectiveBox(boxNumber: String): Boolean {
        return currentBoxDiscrepancies.none {
            it.boxNumber == boxNumber
                    && (it.typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM || it.typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS)
        }
    }

    fun cleanBoxInfo(boxNumber: String, typeDiscrepancies: String, processingUnitNumber: String) {
        currentExciseStampsDiscrepancies.map { it }.filter { unitInfo ->
            if (unitInfo.materialNumber == productInfo.materialNumber
                    && unitInfo.processingUnitNumber == processingUnitNumber
                    && unitInfo.boxNumber == boxNumber
                    && unitInfo.typeDiscrepancies == typeDiscrepancies) {
                currentExciseStampsDiscrepancies.remove(unitInfo)
                return@filter true
            }
            return@filter false
        }

        taskManager.getReceivingTask()
                ?.taskRepository
                ?.getExciseStampsDiscrepancies()
                ?.deleteExciseStampDiscrepancyOfProductOfBoxOfDiscrepancy(
                        materialNumber = productInfo.materialNumber,
                        processingUnitNumber = processingUnitNumber,
                        boxNumber = boxNumber,
                        typeDiscrepancies = typeDiscrepancies
                )

        currentBoxDiscrepancies.map { it }.filter { unitInfo ->
            if (unitInfo.materialNumber == productInfo.materialNumber
                    && unitInfo.boxNumber == boxNumber
                    && unitInfo.typeDiscrepancies == typeDiscrepancies
                    &&unitInfo.processingUnitNumber == processingUnitNumber
            ) {
                currentBoxDiscrepancies.remove(unitInfo)
                return@filter true
            }
            return@filter false
        }

        taskManager
                .getReceivingTask()
                ?.taskRepository
                ?.getBoxesDiscrepancies()
                ?.deleteBoxDiscrepancies(
                        materialNumber = productInfo.materialNumber,
                        boxNumber = boxNumber,
                        typeDiscrepancies = typeDiscrepancies,
                        processingUnitNumber = processingUnitNumber
                )

        taskManager
                .getReceivingTask()
                ?.taskRepository
                ?.getProductsDiscrepancies()
                ?.deleteProductDiscrepancyOfProcessingUnit(
                        materialNumber = productInfo.materialNumber,
                        typeDiscrepancies = typeDiscrepancies,
                        processingUnitNumber = processingUnitNumber
                )
    }

    fun setInitialCount(count: Double) {
        initialCount = count
    }

    fun getInitialCount(): Double {
        return initialCount
    }

    fun decreaseByOneInitialCount() {
        initialCount -= 1
    }

    fun setCountAcceptRefusal(count: Double) {
        countAcceptRefusal = count
    }

    fun getCountAcceptRefusal(): Double {
        return countAcceptRefusal
    }

    fun getGoodsDetails(boxNumber: String) : List<TaskProductDiscrepancies>? {
        val taskRepository = taskManager.getReceivingTask()?.taskRepository
        val boxDiscrepancies =
                taskRepository
                        ?.let { repository ->
                            repository
                                    .getBoxesDiscrepancies()
                                    .findBoxesDiscrepanciesOfProduct(productInfo)
                                    .filter { findBoxes -> findBoxes.boxNumber == boxNumber }
                                    .groupBy { it.typeDiscrepancies }
                        }
                        .orEmpty()

        val stampDiscrepancies =
                        taskRepository
                                ?.let { repository ->
                                    repository
                                            .getExciseStampsDiscrepancies()
                                            .findExciseStampsDiscrepanciesOfProduct(productInfo)
                                            .filter { findStamps -> findStamps.boxNumber == boxNumber }
                                            .groupBy { it.typeDiscrepancies }
                                }
                                .orEmpty()

        val allDiscrepancies = boxDiscrepancies + stampDiscrepancies
        val goodsDetails: ArrayList<TaskProductDiscrepancies> = ArrayList()

        allDiscrepancies.forEach {
            goodsDetails.add(
                    TaskProductDiscrepancies(
                            materialNumber = productInfo.materialNumber,
                            processingUnitNumber = "",
                            numberDiscrepancies = it.value.size.toString(),
                            uom = Uom(code = "", name = ""),
                            typeDiscrepancies = it.key,
                            isNotEdit = false,
                            isNew = false,
                            notEditNumberDiscrepancies = ""
                    )
            )
        }

        return goodsDetails
    }

    fun delBoxStampsDiscrepancies(boxNumber: String, typeDiscrepancies: String) {
        currentBoxDiscrepancies
                .removeItemFromListWithPredicate {
                    it.boxNumber == boxNumber
                            && it.typeDiscrepancies == typeDiscrepancies
                }

        currentExciseStampsDiscrepancies
                .removeItemFromListWithPredicate {
                    it.boxNumber == boxNumber
                            && it.typeDiscrepancies == typeDiscrepancies
                }
    }

    fun delBoxesStampsDiscrepancies(typeDiscrepancies: String) {
        currentBoxDiscrepancies.removeItemFromListWithPredicate { it.typeDiscrepancies == typeDiscrepancies }
        currentExciseStampsDiscrepancies.removeItemFromListWithPredicate { it.typeDiscrepancies == typeDiscrepancies }
    }

}