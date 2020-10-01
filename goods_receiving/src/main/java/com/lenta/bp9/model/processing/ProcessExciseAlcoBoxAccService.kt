package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.shared.di.AppScope
import com.lenta.shared.models.core.ProductType
import javax.inject.Inject

@AppScope
class ProcessExciseAlcoBoxAccService
@Inject constructor() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    private lateinit var productInfo: TaskProductInfo
    private val boxes: ArrayList<TaskBoxInfo> = ArrayList()
    private val currentBoxDiscrepancies: ArrayList<TaskBoxDiscrepancies> = ArrayList()
    private val exciseStamps: ArrayList<TaskExciseStampInfo> = ArrayList()
    private val currentExciseStampsDiscrepancies: ArrayList<TaskExciseStampDiscrepancies> = ArrayList()
    private val currentExciseStampsBad: ArrayList<TaskExciseStampBad> = ArrayList()
    private var countAccept: Double = 0.0

    fun newProcessExciseAlcoBoxService(productInfo: TaskProductInfo): ProcessExciseAlcoBoxAccService? {
        return if (productInfo.type == ProductType.ExciseAlcohol && productInfo.isBoxFl) { //алкоголь, коробочный учет https://trello.com/c/KbBbXj2t
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
            currentExciseStampsBad.clear()
            taskManager.getReceivingTask()?.taskRepository?.getExciseStampsBad()?.getExciseStampsBad()?.map {
                currentExciseStampsBad.add(it.copy())
            }
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
                taskManager
                        .getReceivingTask()
                        ?.taskRepository
                        ?.getExciseStampsDiscrepancies()
                        ?.changeExciseStampDiscrepancy(it)
            }
        }

        if (currentExciseStampsBad.isNotEmpty()) {
            currentExciseStampsBad.map {
                taskManager
                        .getReceivingTask()
                        ?.taskRepository
                        ?.getExciseStampsBad()
                        ?.changeExciseStampBad(it)
            }
        }
    }

    fun addProduct(count: String, typeDiscrepancies: String) {
        val countAdd = if (typeDiscrepancies == "1") count.toDouble() else getCountOfDiscrepanciesOfProduct(typeDiscrepancies) + count.toDouble()

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

        taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.changeProductDiscrepancyOfProcessingUnit(foundDiscrepancy)
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

        currentExciseStampsDiscrepancies.map { it }.filter { unitInfo ->
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

        currentBoxDiscrepancies.map { it }.filter { unitInfo ->
            if (unitInfo.boxNumber == boxNumber) {
                currentBoxDiscrepancies.remove(unitInfo)
                return@filter true
            }
            return@filter false
        }

        currentBoxDiscrepancies.add(foundBoxDiscrepancy)
    }

    fun addDiscrepancyScannedMarkCurrentBox(currentBoxNumber: String, realBoxNumber: String, scannedExciseStampInfo: TaskExciseStampInfo, typeDiscrepancies: String) {
        //https://trello.com/c/Wr4xe6L8 - отмечаем текущий короб и короб, в котором числится отсканированная марка, и все марки из этих коробов, категорией для брака из параметра GRZ_CR_GRUNDCAT
        //удаляем все ранее отсканированные марки для этих коробв
        currentExciseStampsDiscrepancies.map { it }.filter { unitInfo ->
            if (unitInfo.boxNumber == currentBoxNumber || unitInfo.boxNumber == realBoxNumber) {
                currentExciseStampsDiscrepancies.remove(unitInfo)
                return@filter true
            }
            return@filter false
        }

        //отмечаем текущий короб и короб, в котором числится отсканированная марка, категорией для брака из параметра GRZ_CR_GRUNDCAT
        addBoxDiscrepancy(currentBoxNumber, typeDiscrepancies, false) //передаем false, т.к. эта ф-ция (addDiscrepancyScannedMarkCurrentBox) вызывается только при скане марки, а не короба
        addBoxDiscrepancy(realBoxNumber, typeDiscrepancies, false) //передаем false, т.к. эта ф-ция (addDiscrepancyScannedMarkCurrentBox) вызывается только при скане марки, а не короба

        //отмечаем все марки из этих коробов признаком IS_SCAN=false (в карточке трелло не было указано ставить true для этих марок) и категорией для брака из параметра GRZ_CR_GRUNDCAT
        exciseStamps.filter { stamp ->
            stamp.boxNumber == currentBoxNumber || stamp.boxNumber == realBoxNumber
        }.map {
            if (it.code == scannedExciseStampInfo.code) {
                //отмечаем отсканированную марку признаком IS_SCAN=true (так указано в карточке трелло) и категорией для брака из параметра GRZ_CR_GRUNDCAT
                addExciseStampDiscrepancy(it, typeDiscrepancies, true)
            } else {
                //отмечаем все марки, кроме отсканированной, из этих коробов признаком IS_SCAN=false (в карточке трелло не было указано ставить true для этих марок) и категорией для брака из параметра GRZ_CR_GRUNDCAT
                addExciseStampDiscrepancy(it, typeDiscrepancies, false)
            }
        }
    }

    fun denialOfFullProductAcceptance(typeDiscrepancies: String) {
        //https://trello.com/c/WeGFSdAW https://trello.com/c/Vb4JOS5f
        //отмечаем все короба для продукта категорией для брака из параметра GRZ_CR_GRUNDCAT
        boxes.filter { box ->
            box.materialNumber == productInfo.materialNumber
        }.forEach {
            addBoxDiscrepancy(
                    boxNumber = it.boxNumber,
                    typeDiscrepancies = typeDiscrepancies,
                    isScan = false //передаем false, т.к. эта ф-ция (denialOfFullProductAcceptance) вызывается с экрана Обнаружены расхождения по клику на короб на вкладке Не обработаны
            )
        }

        //отмечаем все марки для продукта категорией для брака из параметра GRZ_CR_GRUNDCAT
       /** exciseStamps закомичено согласно этой карточке https://trello.com/c/Vb4JOS5f
                .filter { foundStamp ->
                    foundStamp.materialNumber == productInfo.materialNumber
                }
                .forEach { stamp ->
                    //передаем false, т.к. эта ф-ция (denialOfFullProductAcceptance) вызывается с экрана Обнаружены расхождения по клику на короб на вкладке Не обработаны
                    addExciseStampDiscrepancy(stamp, typeDiscrepancies, false)
                }*/

        //отмечаем продукт
        addProduct(productInfo.origQuantity, typeDiscrepancies)

        //принять все изменения
        applyBoxCard()
    }

    fun refusalToAcceptPartlyByProduct(typeDiscrepancies: String) {
        //https://trello.com/c/WeGFSdAW
        //отмечаем все не обработанные короба для продукта категорией для брака из параметра GRZ_CR_GRUNDCAT
        boxes.filter { box ->
            val currentBoxNumber = currentBoxDiscrepancies.findLast { it.boxNumber == box.boxNumber }?.boxNumber.orEmpty()
            box.materialNumber == productInfo.materialNumber
                    && currentBoxNumber.isEmpty()
        }.forEach {
            addBoxDiscrepancy(
                    boxNumber = it.boxNumber,
                    typeDiscrepancies = typeDiscrepancies,
                    isScan = false //передаем false, т.к. эта ф-ция (refusalToAcceptPartlyByProduct) вызывается с экрана Оюнаружены расхождения по клику на короб на вкладке Не обработаны
            )
        }

        //отмечаем все не обработанные марки для продукта категорией для брака из параметра GRZ_CR_GRUNDCAT
        /**exciseStamps закомичено согласно этой карточке https://trello.com/c/Vb4JOS5f
                .filter { foundStamp ->
                    val currentExciseStampsCode = currentExciseStampsDiscrepancies.findLast { it.code == foundStamp.code }?.code.orEmpty()
                    foundStamp.materialNumber == productInfo.materialNumber
                            && currentExciseStampsCode.isEmpty()
                }
                .forEach { stamp ->
                    addExciseStampDiscrepancy(stamp, typeDiscrepancies, false)//передаем false, т.к. эта ф-ция (refusalToAcceptPartlyByProduct) вызывается с экрана Оюнаружены расхождения по клику на короб на вкладке Не обработаны
                }*/

        //отмечаем продукт
        val countAddProduct = (productInfo.origQuantity.toDouble() - getCountAcceptOfProduct() - getCountRefusalOfProduct()).toString()
        addProduct(countAddProduct, typeDiscrepancies)

        //принять все изменения
        applyBoxCard()
    }

    fun massProcessingRejectBoxes(typeDiscrepancies: String) {
        boxes.filter { box ->
            box.materialNumber == productInfo.materialNumber && currentBoxDiscrepancies.findLast { it.boxNumber == box.boxNumber }?.boxNumber.isNullOrEmpty()
        }.map { unitInfo ->
            addBoxDiscrepancy(unitInfo.boxNumber, typeDiscrepancies, false) //передаем  false, т.к. эта ф-ция (massProcessingRejectBoxes) вызыывается только на экране Информация о товаре и только при нажатии на кнопку Применить и только для массовой обработки брака
            currentBoxDiscrepancies.filter { boxDiscrepancies ->
                boxDiscrepancies.boxNumber == unitInfo.boxNumber
            }.map { addBoxDiscrepancies ->
                taskManager
                        .getReceivingTask()
                        ?.taskRepository
                        ?.getBoxesDiscrepancies()
                        ?.changeBoxDiscrepancy(addBoxDiscrepancies)
            }
        }
    }

    fun overLimit(count: Double): Boolean {
        return productInfo.origQuantity.toDouble() < (getCountAcceptOfProduct() + getCountRefusalOfProduct() + count)
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

    fun getCountBoxOfProductOfDiscrepancies(boxNumber: String, typeDiscrepancies: String): Int {
        return currentBoxDiscrepancies.filter {
            it.boxNumber == boxNumber && it.typeDiscrepancies == typeDiscrepancies
        }.size
    }

    private fun getCountOfDiscrepanciesOfProduct(typeDiscrepancies: String): Double {
        return taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().findProductDiscrepanciesOfProduct(productInfo).filter { productDiscrepancies ->
            productDiscrepancies.typeDiscrepancies == typeDiscrepancies
        }.sumByDouble {
            it.numberDiscrepancies.toDouble()
        }
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

    fun getCountExciseStampDiscrepanciesOfBox(boxNumber: String, typeDiscrepancies: String): Int {
        return currentExciseStampsDiscrepancies.filter {
            it.boxNumber == boxNumber && it.typeDiscrepancies == typeDiscrepancies && it.isScan
        }.size
    }

    //https://trello.com/c/Hve509E5 контроль марки для короба
    fun stampControlOfBox(box: TaskBoxInfo): Boolean {
        val countScannedExciseStampsDiscrepanciesOfBox = currentExciseStampsDiscrepancies.filter {
            it.boxNumber == box.boxNumber && it.typeDiscrepancies == "1" && it.isScan
        }.size

        return countScannedExciseStampsDiscrepanciesOfBox >= productInfo.numberStampsControl.toInt()
    }

    // https://trello.com/c/Hve509E5 контроль короба
    fun boxControl(box: TaskBoxInfo): Boolean {
        val countProcessedBox = currentBoxDiscrepancies.filter {
            it.isScan
                    && it.boxNumber == box.boxNumber
        }.size
        val countScannedExciseStampOfBox = currentExciseStampsDiscrepancies.filter {
            it.boxNumber == box.boxNumber
                    && it.isScan
                    && it.typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
        }.size

        return (countProcessedBox >= 1
                && countScannedExciseStampOfBox >= 1)
                || (countScannedExciseStampOfBox >= 2)
                || (countScannedExciseStampOfBox >= productInfo.numberStampsControl.toInt())
    }

    fun rollbackScannedExciseStamp() {
        val stamp = currentExciseStampsDiscrepancies.last {
            it.isScan
        }
        currentExciseStampsDiscrepancies.remove(stamp)
    }

    fun getCountUntreatedBoxes(): Int {
        val countTotalBoxes = boxes.filter { it.materialNumber == productInfo.materialNumber }.size
        val countProcessedBoxes = currentBoxDiscrepancies.filter { it.materialNumber == productInfo.materialNumber }.size
        return countTotalBoxes - countProcessedBoxes
    }

    fun getCountDefectBoxes(): Int {
        return currentBoxDiscrepancies.filter { it.materialNumber == productInfo.materialNumber && it.typeDiscrepancies != "1" }.size
    }

    fun modifications(): Boolean {
        return currentExciseStampsDiscrepancies != taskManager.getReceivingTask()?.taskRepository?.getExciseStampsDiscrepancies()?.getExciseStampDiscrepancies()
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

    fun defectiveBox(boxNumber: String): Boolean {
        return currentBoxDiscrepancies.none {
            it.boxNumber == boxNumber && it.typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
        }
    }

    fun getCountBoxesProcessedOfProduct(): Int {
        return currentBoxDiscrepancies.filter {
            it.materialNumber == productInfo.materialNumber
        }.size
    }

    fun cleanBoxInfo(boxNumber: String, typeDiscrepancies: String) {
        currentExciseStampsDiscrepancies
                .map { it }
                .filter { unitInfo ->
                    if (unitInfo.materialNumber == productInfo.materialNumber
                            && unitInfo.boxNumber == boxNumber
                            && unitInfo.typeDiscrepancies == typeDiscrepancies) {
                        currentExciseStampsDiscrepancies.remove(unitInfo)
                        return@filter true
                    }
                    return@filter false
                }

        taskManager
                .getReceivingTask()
                ?.taskRepository
                ?.getExciseStampsDiscrepancies()
                ?.deleteExciseStampDiscrepancyOfProductOfBoxOfDiscrepancy(
                        materialNumber = productInfo.materialNumber,
                        boxNumber = boxNumber,
                        typeDiscrepancies = typeDiscrepancies,
                        processingUnitNumber = productInfo.processingUnit
                )

        currentBoxDiscrepancies
                .map { it }
                .filter { unitInfo ->
                    if (unitInfo.materialNumber == productInfo.materialNumber
                            && unitInfo.boxNumber == boxNumber
                            && unitInfo.typeDiscrepancies == typeDiscrepancies) {
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
                        processingUnitNumber = productInfo.processingUnit
                )

        taskManager
                .getReceivingTask()
                ?.taskRepository
                ?.getProductsDiscrepancies()
                ?.deleteProductDiscrepancy(productInfo.materialNumber, typeDiscrepancies)
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

    fun setCountAccept(count: Double) {
        countAccept = count
    }

    fun getCountAccept(): Double {
        return countAccept
    }


    fun searchCurrentBoxDiscrepancies(boxNumber: String): TaskBoxDiscrepancies? {
        return currentBoxDiscrepancies.findLast {
            it.boxNumber == boxNumber
        }
    }

}