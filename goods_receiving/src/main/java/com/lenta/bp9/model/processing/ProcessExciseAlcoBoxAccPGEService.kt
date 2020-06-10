package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.*
import com.lenta.shared.di.AppScope
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.utilities.extentions.toStringFormatted
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
    private val currentExciseStampsBad: ArrayList<TaskExciseStampBad> = ArrayList()
    private var initialCount: Double = 0.0
    private var countAcceptRefusal: Double = 0.0

    fun newProcessExciseAlcoBoxPGEService(productInfo: TaskProductInfo) : ProcessExciseAlcoBoxAccPGEService? {
        return if (productInfo.type == ProductType.ExciseAlcohol && productInfo.isBoxFl){ //алкоголь, коробочный учет https://trello.com/c/TzUSGIH7
            this.productInfo = productInfo.copy()
            boxes.clear()
            taskManager.getReceivingTask()?.taskRepository?.getBoxes()?.getBoxes()?.map {
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
            currentExciseStampsBad.clear()
            taskManager.getReceivingTask()?.taskRepository?.getExciseStampsBad()?.getExciseStampsBad()?.map {
                currentExciseStampsBad.add(it.copy())
            }
            initialCount = 0.0
            countAcceptRefusal = 0.0
            this
        }
        else null
    }

    fun applyBoxCard(box: TaskBoxInfo, typeDiscrepancies: String, isScan: Boolean) {
        addBoxDiscrepancy(box.boxNumber, typeDiscrepancies, isScan)
        if (currentBoxDiscrepancies.isNotEmpty()) {
            currentBoxDiscrepancies.map {
                taskManager.getReceivingTask()?.
                        taskRepository?.
                        getBoxesDiscrepancies()?.
                        changeBoxDiscrepancy(it)
            }
        }

        if (currentExciseStampsDiscrepancies.isNotEmpty()) {
            currentExciseStampsDiscrepancies.map {
                taskManager.getReceivingTask()?.
                        taskRepository?.
                        getExciseStampsDiscrepancies()?.
                        changeExciseStampDiscrepancy(it)
            }
        }

        if (currentExciseStampsBad.isNotEmpty()) {
            currentExciseStampsBad.map {
                taskManager.getReceivingTask()?.
                        taskRepository?.
                        getExciseStampsBad()?.
                        changeExciseStampBad(it)
            }
        }
    }

    fun addProduct(count: String, typeDiscrepancies: String){
        val countAdd = getCountOfDiscrepanciesOfProduct(typeDiscrepancies) + count.toDouble()

        //добавляем кол-во по расхождению для продукта
        var foundDiscrepancy = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().findProductDiscrepanciesOfProduct(productInfo).findLast {
            it.typeDiscrepancies == typeDiscrepancies
        }

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

        taskManager.getReceivingTask()?.
                taskRepository?.
                getProductsDiscrepancies()?.
                changeProductDiscrepancy(foundDiscrepancy)
    }

    fun addExciseStampDiscrepancy(exciseStamp: TaskExciseStampInfo, typeDiscrepancies: String, isScan: Boolean) {
        var foundExciseStampDiscrepancy = currentExciseStampsDiscrepancies.findLast {
            it.code == exciseStamp.code
        }

        foundExciseStampDiscrepancy = foundExciseStampDiscrepancy?.copy(typeDiscrepancies = typeDiscrepancies, isScan = isScan)
                ?: TaskExciseStampDiscrepancies(
                        materialNumber = exciseStamp.materialNumber,
                        code = exciseStamp.code,
                        processingUnitNumber = exciseStamp.processingUnitNumber,
                        typeDiscrepancies = typeDiscrepancies,
                        isScan = isScan,
                        boxNumber = exciseStamp.boxNumber,
                        packNumber = "",
                        isMSC = false,
                        organizationCodeEGAIS = exciseStamp.organizationCodeEGAIS,
                        bottlingDate = exciseStamp.bottlingDate,
                        isUnknown = false
                )

        currentExciseStampsDiscrepancies.map { it }.filter {unitInfo ->
            if (unitInfo.code == exciseStamp.code) {
                currentExciseStampsDiscrepancies.remove(unitInfo)
                return@filter true
            }
            return@filter false
        }

        currentExciseStampsDiscrepancies.add(foundExciseStampDiscrepancy)
    }

    fun addExciseStampBad(exciseStampCode: String) {
        var index = -1
        for (i in currentExciseStampsBad.indices) {
            if (exciseStampCode == currentExciseStampsBad[i].exciseStampCode) {
                index = i
            }
        }

        if (index == -1) {
            currentExciseStampsBad.add(TaskExciseStampBad(
                    materialNumber = "",
                    exciseStampCode = exciseStampCode,
                    processingUnitNumber = "",
                    typeDiscrepancies = "",
                    isScan = true,
                    boxNumber = ""
            ))
        }
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

        currentBoxDiscrepancies.map { it }.filter {unitInfo ->
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
        exciseStamps.filter {stamp ->
            stamp.boxNumber == boxNumber
        }.map {
            addExciseStampDiscrepancy(it, typeDiscrepancies, false)
        }

    }

    fun addDiscrepancyScannedMarkCurrentBox(currentBoxNumber: String, realBoxNumber: String, scannedExciseStampInfo: TaskExciseStampInfo, typeDiscrepancies: String) {
        //https://trello.com/c/E4b0z0q5 2.1. Сохранять отсканированную марку коробке, в которой она числится как "Норма";
        addExciseStampDiscrepancy(scannedExciseStampInfo, typeDiscrepancies, true)
    }

    fun overLimit(count: Double) : Boolean {
        return productInfo.orderQuantity.toDouble() < (getCountAcceptOfProduct() + getCountRefusalOfProduct() + count)
    }

    fun searchExciseStamp(code: String) : TaskExciseStampInfo? {
        return exciseStamps.findLast {
            it.code == code
        }
    }

    fun exciseStampIsAlreadyProcessed(code: String) : Boolean {
        return currentExciseStampsDiscrepancies.any {
            it.code == code && it.isScan
        }
    }

    fun searchBox(boxNumber: String) : TaskBoxInfo? {
        return boxes.findLast {
            it.boxNumber == boxNumber
        }
    }

    fun getCountBoxOfProductOfDiscrepancies(boxNumber: String) : Int {
        return currentBoxDiscrepancies.filter {
            it.boxNumber == boxNumber
        }.size
    }

    private fun getCountOfDiscrepanciesOfProduct(typeDiscrepancies: String) : Double {
        return taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().findProductDiscrepanciesOfProduct(productInfo).filter {productDiscrepancies ->
            productDiscrepancies.typeDiscrepancies == typeDiscrepancies
        }.sumByDouble {
            it.numberDiscrepancies.toDouble()
        }
    }

    fun getCountAcceptOfProduct() : Double {
        return taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().findProductDiscrepanciesOfProduct(productInfo).filter {productDiscrepancies ->
            productDiscrepancies.typeDiscrepancies == "1" || productDiscrepancies.typeDiscrepancies == "2"
        }.sumByDouble {
            it.numberDiscrepancies.toDouble()
        }
    }

    fun getCountRefusalOfProduct() : Double {
        return taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().findProductDiscrepanciesOfProduct(productInfo).filter {productDiscrepancies ->
            productDiscrepancies.typeDiscrepancies == "3" || productDiscrepancies.typeDiscrepancies == "4" || productDiscrepancies.typeDiscrepancies == "5"
        }.sumByDouble {
            it.numberDiscrepancies.toDouble()
        }
    }

    fun getCountExciseStampDiscrepanciesOfBox(boxNumber: String) : Int {
        return currentExciseStampsDiscrepancies.filter {
            it.boxNumber == boxNumber && it.isScan
        }.size
    }

    //контроль марки для короба
    fun stampControlOfBox(box: TaskBoxInfo) : Boolean {
        val countScannedExciseStampsDiscrepanciesOfBox = currentExciseStampsDiscrepancies.filter {
            it.boxNumber == box.boxNumber && it.isScan
        }.size

        return countScannedExciseStampsDiscrepanciesOfBox >= productInfo.numberStampsControl.toInt()
    }

    // контроль короба
    fun boxControl(box: TaskBoxInfo) : Boolean {
        val countProcessedBox = currentBoxDiscrepancies.filter {
            it.boxNumber == box.boxNumber
        }.size
        val countScannedExciseStampOfBox = currentExciseStampsDiscrepancies.filter {
            it.boxNumber == box.boxNumber && it.isScan
        }.size

        return (countProcessedBox >=1 && countScannedExciseStampOfBox >=1) || (countScannedExciseStampOfBox >= 2) || (countScannedExciseStampOfBox >= productInfo.numberStampsControl.toInt())
    }

    fun rollbackScannedExciseStamp() {
        val stamp = currentExciseStampsDiscrepancies.last {
            it.isScan
        }
        currentExciseStampsDiscrepancies.remove(stamp)
    }

    fun getLastAddExciseStamp() : TaskExciseStampInfo? {
        return if (currentExciseStampsDiscrepancies.isNotEmpty()) {
            exciseStamps.findLast { stampInfo ->
                stampInfo.code == currentExciseStampsDiscrepancies.last {
                    it.isScan
                }.code
            }
        } else null
    }

    fun getCountUntreatedBoxes() : Int {
        val countTotalBoxes = boxes.filter { it.materialNumber == productInfo.materialNumber }.size
        val countProcessedBoxes = currentBoxDiscrepancies.filter { it.materialNumber == productInfo.materialNumber }.size
        return countTotalBoxes - countProcessedBoxes
    }

    fun modifications() : Boolean {
        return currentExciseStampsDiscrepancies != taskManager.getReceivingTask()?.taskRepository?.getExciseStampsDiscrepancies()?.getExciseStampDiscrepancies() ||
                currentBoxDiscrepancies != taskManager.getReceivingTask()?.taskRepository?.getBoxesDiscrepancies()?.getBoxesDiscrepancies()
    }

    fun boxProcessed(boxNumber: String) : Boolean {
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
        currentExciseStampsBad.clear()
        taskManager.getReceivingTask()?.taskRepository?.getExciseStampsBad()?.getExciseStampsBad()?.map {
            currentExciseStampsBad.add(it.copy())
        }
    }

    fun defectiveBox (boxNumber: String) : Boolean {
        return currentBoxDiscrepancies.none {
            it.boxNumber == boxNumber && (it.typeDiscrepancies == "1" || it.typeDiscrepancies == "2")
        }
    }

    fun cleanBoxInfo(boxNumber: String, typeDiscrepancies: String) {
        currentExciseStampsDiscrepancies.map { it }.filter {unitInfo ->
            if (unitInfo.materialNumber == productInfo.materialNumber && unitInfo.boxNumber == boxNumber && unitInfo.typeDiscrepancies == typeDiscrepancies) {
                currentExciseStampsDiscrepancies.remove(unitInfo)
                return@filter true
            }
            return@filter false
        }

        taskManager.getReceivingTask()?.
                taskRepository?.
                getExciseStampsDiscrepancies()?.
                deleteExciseStampDiscrepancyOfProductOfBoxOfDiscrepancy(
                        materialNumber = productInfo.materialNumber,
                        boxNumber = boxNumber,
                        typeDiscrepancies = typeDiscrepancies
                )

        currentBoxDiscrepancies.map { it }.filter {unitInfo ->
            if (unitInfo.materialNumber == productInfo.materialNumber && unitInfo.boxNumber == boxNumber && unitInfo.typeDiscrepancies == typeDiscrepancies) {
                currentBoxDiscrepancies.remove(unitInfo)
                return@filter true
            }
            return@filter false
        }

        taskManager.getReceivingTask()?.
                taskRepository?.
                getBoxesDiscrepancies()?.
                deleteBoxDiscrepancies(
                        materialNumber = productInfo.materialNumber,
                        boxNumber = boxNumber,
                        typeDiscrepancies = typeDiscrepancies
                )

        taskManager
                .getReceivingTask()
                ?.taskRepository
                ?.getProductsDiscrepancies()
                ?.deleteProductDiscrepancy(productInfo.materialNumber, typeDiscrepancies)
    }

    fun setInitialCount(count: Double) {
        initialCount = count
    }

    fun getInitialCount() : Double {
        return initialCount
    }

    fun decreaseByOneInitialCount() {
        initialCount -= 1
    }

    fun setCountAcceptRefusal(count: Double) {
        countAcceptRefusal = count
    }

    fun getCountAcceptRefusal() : Double {
        return countAcceptRefusal
    }

}