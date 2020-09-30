package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.*
import com.lenta.shared.di.AppScope
import com.lenta.shared.models.core.ProductType
import javax.inject.Inject

@AppScope
class ProcessExciseAlcoStampAccPGEService @Inject constructor() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    private lateinit var productInfo: TaskProductInfo
    private val exciseStamps: ArrayList<TaskExciseStampInfo> = ArrayList()
    private val currentExciseStampsDiscrepancies: ArrayList<TaskExciseStampDiscrepancies> = ArrayList()

    fun newProcessExciseAlcoStampPGEService(productInfo: TaskProductInfo): ProcessExciseAlcoStampAccPGEService? {
        return if (productInfo.type == ProductType.ExciseAlcohol && productInfo.isMarkFl) { //алкоголь, марочный учет ПГЕ https://trello.com/c/Bx03dgxE
            this.productInfo = productInfo.copy()
            exciseStamps.clear()
            taskManager.getReceivingTask()?.taskRepository?.getExciseStamps()?.getExciseStamps()?.map {
                exciseStamps.add(it.copy())
            }
            currentExciseStampsDiscrepancies.clear()
            taskManager.getReceivingTask()?.taskRepository?.getExciseStampsDiscrepancies()?.findExciseStampsDiscrepanciesOfProduct(productInfo)?.map {
                currentExciseStampsDiscrepancies.add(it.copy())
            }
            this
        } else null
    }

    fun setProcessingUnitNumber(processingUnitNumber: String) {
        productInfo = productInfo.copy(processingUnit = processingUnitNumber)
        taskManager.getReceivingTask()?.taskRepository?.getProducts()?.changeProduct(productInfo)
    }

    fun filterAndUpdateBlockDiscrepansies() {
        if (currentExciseStampsDiscrepancies.isNotEmpty()) {
            currentExciseStampsDiscrepancies.map {
                taskManager.getReceivingTask()?.taskRepository?.getExciseStampsDiscrepancies()?.changeExciseStampDiscrepancy(it)
            }
        }
    }

    fun addProduct(count: String, typeDiscrepancies: String) {
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

        taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.changeProductDiscrepancy(foundDiscrepancy)
    }

    fun addExciseStampDiscrepancy(exciseStamp: TaskExciseStampInfo?, typeDiscrepancies: String, isScan: Boolean) {
        var foundExciseStampDiscrepancy = currentExciseStampsDiscrepancies.findLast {
            it.code == exciseStamp?.code.orEmpty()
        }

        foundExciseStampDiscrepancy = foundExciseStampDiscrepancy?.copy(typeDiscrepancies = typeDiscrepancies, isScan = isScan)
                ?: TaskExciseStampDiscrepancies(
                        materialNumber = exciseStamp?.materialNumber.orEmpty(),
                        code = exciseStamp?.code.orEmpty(),
                        processingUnitNumber = exciseStamp?.processingUnitNumber.orEmpty(),
                        typeDiscrepancies = typeDiscrepancies,
                        isScan = isScan,
                        boxNumber = exciseStamp?.boxNumber.orEmpty(),
                        packNumber = "",
                        isMSC = false,
                        organizationCodeEGAIS = exciseStamp?.organizationCodeEGAIS.orEmpty(),
                        bottlingDate = exciseStamp?.bottlingDate.orEmpty(),
                        isUnknown = false
                )

        currentExciseStampsDiscrepancies.map { it }.filter { unitInfo ->
            if (unitInfo.code == exciseStamp?.code) {
                currentExciseStampsDiscrepancies.remove(unitInfo)
                return@filter true
            }
            return@filter false
        }

        currentExciseStampsDiscrepancies.add(foundExciseStampDiscrepancy)
    }

    fun overLimit(count: Double): Boolean {
        return productInfo.orderQuantity.toDouble() < (getCountAcceptOfProduct() + getCountRefusalOfProduct() + count)
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

    fun getLastAddExciseStamp(): TaskExciseStampInfo? {
        return if (currentExciseStampsDiscrepancies.isNotEmpty()) {
            exciseStamps.findLast { stampInfo ->
                stampInfo.code == currentExciseStampsDiscrepancies.last {
                    it.isScan
                }.code
            }
        } else null
    }

    private fun getCountOfDiscrepanciesOfProduct(typeDiscrepancies: String): Double {
        return taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().findProductDiscrepanciesOfProduct(productInfo).filter { productDiscrepancies ->
            productDiscrepancies.typeDiscrepancies == typeDiscrepancies
        }.sumByDouble {
            it.numberDiscrepancies.toDouble()
        }
    }

    fun getCountAcceptOfProduct(): Double {
        return taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().findProductDiscrepanciesOfProduct(productInfo).filter { productDiscrepancies ->
            productDiscrepancies.typeDiscrepancies == "1" || productDiscrepancies.typeDiscrepancies == "2"
        }.sumByDouble {
            it.numberDiscrepancies.toDouble()
        }
    }

    fun getCountRefusalOfProduct(): Double {
        return taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().findProductDiscrepanciesOfProduct(productInfo).filter { productDiscrepancies ->
            productDiscrepancies.typeDiscrepancies == "3" || productDiscrepancies.typeDiscrepancies == "4" || productDiscrepancies.typeDiscrepancies == "5"
        }.sumByDouble {
            it.numberDiscrepancies.toDouble()
        }
    }

    fun rollbackScannedExciseStamp() {
        val stamp = currentExciseStampsDiscrepancies.last {
            it.isScan
        }
        currentExciseStampsDiscrepancies.remove(stamp)
    }

    fun modifications(): Boolean {
        return currentExciseStampsDiscrepancies != taskManager.getReceivingTask()?.taskRepository?.getExciseStampsDiscrepancies()?.getExciseStampDiscrepancies()
    }

    fun getCountExciseStampsSurplusScanned(): Int {
        return currentExciseStampsDiscrepancies.filter {
            it.isScan && it.typeDiscrepancies == "2"
        }.size
    }

    fun clearModifications() {
        currentExciseStampsDiscrepancies.clear()
        taskManager.getReceivingTask()?.taskRepository?.getExciseStampsDiscrepancies()?.findExciseStampsDiscrepanciesOfProduct(productInfo)?.map {
            currentExciseStampsDiscrepancies.add(it.copy())
        }
    }

}