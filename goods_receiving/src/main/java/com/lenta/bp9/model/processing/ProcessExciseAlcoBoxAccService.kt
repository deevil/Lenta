package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.*
import com.lenta.shared.di.AppScope
import com.lenta.shared.models.core.ProductType
import javax.inject.Inject

@AppScope
class ProcessExciseAlcoBoxAccService
@Inject constructor() {
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    private lateinit var productInfo: TaskProductInfo
    private var currentProductDiscrepancies: ArrayList<TaskProductDiscrepancies> = ArrayList()
    private val currentBoxes: ArrayList<TaskBoxInfo> = ArrayList()
    private val currentBoxDiscrepancies: ArrayList<TaskBoxDiscrepancies> = ArrayList()
    private val currentExciseStamps: ArrayList<TaskExciseStampInfo> = ArrayList()
    private val currentExciseStampsDiscrepancies: ArrayList<TaskExciseStampDiscrepancies> = ArrayList()

    fun newProcessExciseAlcoBoxService(productInfo: TaskProductInfo) : ProcessExciseAlcoBoxAccService? {
        return if (productInfo.type == ProductType.ExciseAlcohol && productInfo.isBoxFl){ //алкоголь, коробочный учет https://trello.com/c/KbBbXj2t
            this.productInfo = productInfo.copy()
            currentProductDiscrepancies.clear()
            taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.map {
                currentProductDiscrepancies.add(it.copy())
            }
            currentBoxes.clear()
            taskManager.getReceivingTask()?.taskRepository?.getBoxes()?.findBoxesOfProduct(productInfo)?.map {
                currentBoxes.add(it.copy())
            }
            currentBoxDiscrepancies.clear()
            taskManager.getReceivingTask()?.taskRepository?.getBoxesDiscrepancies()?.findBoxesDiscrepanciesOfProduct(productInfo)?.map {
                currentBoxDiscrepancies.add(it.copy())
            }
            currentExciseStamps.clear()
            taskManager.getReceivingTask()?.taskRepository?.getExciseStamps()?.findExciseStampsOfProduct(productInfo)?.map {
                currentExciseStamps.add(it.copy())
            }
            currentExciseStampsDiscrepancies.clear()
            taskManager.getReceivingTask()?.taskRepository?.getExciseStampsDiscrepancies()?.findExciseStampsDiscrepanciesOfProduct(productInfo)?.map {
                currentExciseStampsDiscrepancies.add(it.copy())
            }
            this
        }
        else null
    }

    fun add(count: String, typeDiscrepancies: String){
        val countAdd = if (typeDiscrepancies == "1") count.toDouble() else getCountOfDiscrepanciesOfProduct(typeDiscrepancies) + count.toDouble()

        //добавляем кол-во по расхождению для продукта
        var foundDiscrepancy = currentProductDiscrepancies.findLast {
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

        changeCurrentProductDiscrepancy(foundDiscrepancy)

        /**val foundDiscrepancy = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.findLast {
            it.materialNumber == productInfo.materialNumber && it.typeDiscrepancies == typeDiscrepancies
        }

        if (foundDiscrepancy == null) {
            taskManager.getReceivingTask()?.
                    taskRepository?.
                    getProductsDiscrepancies()?.
                    changeProductDiscrepancy(TaskProductDiscrepancies(
                            materialNumber = productInfo.materialNumber,
                            processingUnitNumber = "",
                            numberDiscrepancies = countAdd.toString(),
                            uom = productInfo.uom,
                            typeDiscrepancies = typeDiscrepancies,
                            isNotEdit = false,
                            isNew = false,
                            notEditNumberDiscrepancies = ""
                    ))
        } else {
            taskManager.getReceivingTask()?.
                    taskRepository?.
                    getProductsDiscrepancies()?.
                    changeProductDiscrepancy(foundDiscrepancy.copy(numberDiscrepancies = countAdd.toString()))
        }*/

    }

    fun addExciseStampDiscrepancy(exciseStamp: TaskExciseStampInfo, typeDiscrepancies: String) {
        var index = -1
        for (i in currentExciseStampsDiscrepancies.indices) {
            if (exciseStamp.code == currentExciseStampsDiscrepancies[i].code) {
                index = i
            }
        }

        if (index == -1) {
            currentExciseStampsDiscrepancies.add(TaskExciseStampDiscrepancies(
                    materialNumber = exciseStamp.materialNumber,
                    code = exciseStamp.code,
                    processingUnitNumber = exciseStamp.processingUnitNumber,
                    typeDiscrepancies = typeDiscrepancies,
                    isScan = true,
                    boxNumber = exciseStamp.boxNumber,
                    packNumber = "",
                    isMSC = false,
                    organizationCodeEGAIS = exciseStamp.organizationCodeEGAIS,
                    bottlingDate = exciseStamp.bottlingDate,
                    isUnknown = false
            ))
        }
    }

    private fun changeCurrentProductDiscrepancy(newDiscrepancy: TaskProductDiscrepancies) {
        var index = -1
        for (i in currentProductDiscrepancies.indices) {
            if (newDiscrepancy.typeDiscrepancies == currentProductDiscrepancies[i].typeDiscrepancies) {
                index = i
            }
        }

        if (index != -1) {
            currentProductDiscrepancies.removeAt(index)
        }

        currentProductDiscrepancies.add(newDiscrepancy)
    }

    fun overLimit(count: Double) : Boolean {
        return productInfo.origQuantity.toDouble() < (getCountAcceptOfProduct() + getCountRefusalOfProduct() + count)
    }

    fun searchExciseStamp(code: String) : TaskExciseStampInfo? {
        return currentExciseStamps.findLast {
            it.code == code
        }
    }

    fun searchBox(boxNumber: String) : TaskBoxInfo? {
        return taskManager.getReceivingTask()?.taskRepository?.getBoxes()?.getBoxes()?.findLast {
            it.boxNumber == boxNumber
        }
    }

    fun getCountBoxOfProductOfDiscrepancies(materialNumber: String, boxNumber: String, typeDiscrepancies: String) : Int {
        return taskManager.getReceivingTask()?.taskRepository?.getBoxesDiscrepancies()?.getBoxesDiscrepancies()?.filter {
            it.boxNumber == boxNumber && it.materialNumber == materialNumber && it.typeDiscrepancies == typeDiscrepancies
        }?.size ?: 0
    }

    private fun getCountOfDiscrepanciesOfProduct(typeDiscrepancies: String) : Double {
        return currentProductDiscrepancies.filter {productDiscrepancies ->
            productDiscrepancies.typeDiscrepancies == typeDiscrepancies
        }.sumByDouble {
            it.numberDiscrepancies.toDouble()
        }
    }

    fun getCountAcceptOfProduct() : Double {
        return currentProductDiscrepancies.filter {productDiscrepancies ->
            productDiscrepancies.typeDiscrepancies == "1"
        }.sumByDouble {
            it.numberDiscrepancies.toDouble()
        }
    }

    fun getCountRefusalOfProduct() : Double {
        return currentProductDiscrepancies.filter {productDiscrepancies ->
            productDiscrepancies.typeDiscrepancies != "1"
        }.sumByDouble {
            it.numberDiscrepancies.toDouble()
        }
    }

}