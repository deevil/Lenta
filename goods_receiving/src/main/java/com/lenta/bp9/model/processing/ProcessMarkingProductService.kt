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
    private val exciseStamps: ArrayList<TaskExciseStampInfo> = ArrayList()
    private val currentExciseStampsDiscrepancies: ArrayList<TaskExciseStampDiscrepancies> = ArrayList()
    private val blocks: ArrayList<TaskBlockInfo> = ArrayList()

    fun newProcessMarkingProductService(productInfo: TaskProductInfo) : ProcessMarkingProductService? {
        return if (productInfo.type == ProductType.General){
            this.productInfo = productInfo.copy()
            exciseStamps.clear()
            taskManager.getReceivingTask()?.taskRepository?.getExciseStamps()?.getExciseStamps()?.map {
                exciseStamps.add(it.copy())
            }
            currentExciseStampsDiscrepancies.clear()
            taskManager.getReceivingTask()?.taskRepository?.getExciseStampsDiscrepancies()?.findExciseStampsDiscrepanciesOfProduct(productInfo)?.map {
                currentExciseStampsDiscrepancies.add(it.copy())
            }
            blocks.clear()
            taskManager.getReceivingTask()?.getProcessedBlocks()?.map {
                blocks.add(it.copy())
            }
            this
        }
        else null
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

        taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.changeProductDiscrepancy(foundDiscrepancy)
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

        currentExciseStampsDiscrepancies.map { it }.filter { unitInfo ->
            if (unitInfo.code == exciseStamp.code) {
                currentExciseStampsDiscrepancies.remove(unitInfo)
                return@filter true
            }
            return@filter false
        }

        currentExciseStampsDiscrepancies.add(foundExciseStampDiscrepancy)
    }

    fun overLimit(count: Double): Boolean {
        return productInfo.origQuantity.toDouble() < (getCountAcceptOfProduct() + getCountRefusalOfProduct() + count)
    }

    fun searchExciseStamp(code: String): TaskExciseStampInfo? {
        return exciseStamps.findLast {
            it.code == code
        }
    }

    fun searchBlock(blockNumber: String): TaskBlockInfo? {
        return blocks.findLast {
            it.blockNumber == blockNumber
        }
    }

    fun exciseStampIsAlreadyProcessed(code: String): Boolean {
        return currentExciseStampsDiscrepancies.any {
            it.code == code && it.isScan
        }
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

    fun stampControlOfBox(box: TaskBoxInfo): Boolean {
        val countScannedExciseStampsDiscrepanciesOfBox = currentExciseStampsDiscrepancies.filter {
            it.boxNumber == box.boxNumber && it.typeDiscrepancies == "1" && it.isScan
        }.size

        return countScannedExciseStampsDiscrepanciesOfBox >= productInfo.numberStampsControl.toInt()
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

    fun getLastAddExciseStamp(): TaskExciseStampInfo? {
        return if (currentExciseStampsDiscrepancies.isNotEmpty()) {
            exciseStamps.findLast { stampInfo ->
                stampInfo.code == currentExciseStampsDiscrepancies.last {
                    it.isScan
                }.code
            }
        } else null
    }

}