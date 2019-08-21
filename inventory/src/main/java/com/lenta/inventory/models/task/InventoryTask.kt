package com.lenta.inventory.models.task

import com.lenta.inventory.models.RecountType
import com.lenta.inventory.models.repositories.ITaskRepository
import com.lenta.inventory.requests.network.*

class InventoryTask(val taskDescription: TaskDescription, val taskRepository: ITaskRepository) {

    fun getProcessedStorePlaces(): List<TaskStorePlaceInfo> {
        return taskRepository.getStorePlace().getStorePlaces().filter { it.isProcessed }
    }

    fun getUnprocessedStorePlaces(): List<TaskStorePlaceInfo> {
        return taskRepository.getStorePlace().getStorePlaces().filter { !it.isProcessed }
    }

    fun getProductsQuantityForStorePlace(storePlaceNumber: String): Int {
        return taskRepository.getProducts().getProducts().count { it.placeCode == storePlaceNumber && !it.isDel }
    }

    //Вызывается в двух случаях:
    //1. Нестрогий список, обычный пересчет, товар по умолчанию находится в общем МХ 00
    fun deleteProduct(productNumber: String, storePlaceNumber: String = "00") {
     taskRepository.getProducts().findProduct(productNumber, storePlaceNumber)?.let {
         if (it.isAddedManually) {
             taskRepository.getProducts().deleteProduct(it)
         } else {
             taskRepository.getProducts().changeProduct(it.copy(isDel = true))
         }
        }
    }

    //2. Строгий список, пересчет по МХ, по нажатию "Отвязать", удаляем товар в конкретном заданном МХ
    fun untieProduct(productNumber: String, storePlaceNumber: String) {
        taskRepository.getProducts().findProduct(productNumber, storePlaceNumber)?.let {
            taskRepository.getProducts().untieProduct(it)
            taskRepository.getProducts().deleteProduct(it)
        }
    }


    //Если не передавать номер МХ, то отсутствующими помечаются товары с заданным номером во всех МХ
    fun markProductMissing(productNumber: String, storePlaceNumber: String? = null) {
        if (storePlaceNumber != null) {
            taskRepository.getProducts().findProduct(productNumber, storePlaceNumber)?.let {
                taskRepository.getProducts().changeProduct(it.copy(factCount = 0.0, isPositionCalc = true))
            }
        } else {
            taskRepository.getProducts().getProducts().filter {
                it.materialNumber == productNumber
            }.forEach {
                taskRepository.getProducts().changeProduct(it.copy(factCount = 0.0, isPositionCalc = true))
            }
        }
    }

    fun getReport(isFinish: Boolean, ip: String, personnelNumber: String, isRecount: Boolean): InventoryReport {
        val taskDescription = taskDescription
        return InventoryReport(
                tkNumber = taskDescription.tkNumber,
                ipAdress = ip,
                taskNumber = taskDescription.taskNumber,
                isFinish = if (isFinish) "X" else "",
                personnelNumber = personnelNumber,
                storePlacesForDelete = getUntiedProducts(),
                products = getReportsProducts(),
                stamps = getReportStamps(),
                isRecount = if (isRecount) "X" else ""
        )
    }

    //TODO предварительная версия. Логика будет уточнятся дополнятся
    private fun getReportStamps(): List<ExciseStampInfo> {
        return taskRepository.getExciseStamps()
                .getExciseStamps().map {
                    ExciseStampInfo(
                            productNumber = it.materialNumber,
                            storePlaceCode = it.placeCode,
                            stampNumber = it.code,
                            boxNumber = it.boxNumber,
                            productNumberOSN = it.setMaterialNumber,
                            organizationCodeEGAIS = it.manufacturerCode,
                            dateOfPour = it.bottlingDate,
                            isUnknown = if (it.isBadStamp) "X" else ""
                    )
                }
    }

    //TODO предварительная версия. Логика будет уточнятся дополнятся
    private fun getUntiedProducts(): List<UntiedProduct> {
        return taskRepository
                .getProducts().getUntiedProducts().map {
                    UntiedProduct(
                            storage = taskDescription.tkNumber,
                            placeCode = it.placeCode,
                            matNumber = it.materialNumber,
                            isDel = if (it.isDel) "X" else ""
                    )
                }
    }

    private fun getReportsProducts(): List<MaterialNumber> {
        return (taskRepository.getProducts().getProcessedProducts(includingDeleted = true)).map {
            MaterialNumber(
                    materialNumber = it.materialNumber,
                    storePlaceCode = it.placeCode,
                    factQuantity = it.factCount.toString(),
                    positionCounted = if (it.isPositionCalc) "X" else "",
                    isDel = if (it.isDel) "X" else "",
                    isSet = if (it.isSet) "X" else "",
                    isExcOld = if (it.isExcOld) "X" else ""
            )
        }
    }

    //вызывается при возврате на 20 экран и при нажатии на кнопку ОБНОВИТЬ, вызываем 96 рест
    fun updateStorePlaces(storePlaces: List<TaskStorePlaceInfo>): InventoryTask {
        taskRepository.getStorePlace().updateStorePlaces(storePlaces)
        return this
    }

    fun updateProducts(products: List<TaskProductInfo>): InventoryTask {
        taskRepository.getProducts().updateProducts(products)
        return this
    }

    fun updateExciseStamps(exciseStamps: List<TaskExciseStamp>): InventoryTask {
        taskRepository.getExciseStamps().updateExciseStamps(exciseStamps)
        return this
    }

    fun clearStorePlaceByNumber(storePlaceNumber: String): InventoryTask {
        taskRepository.getStorePlace().findStorePlace(storePlaceNumber)?.let {
            return clearStorePlace(it)
        }
        return this
    }

    fun clearStorePlace(storePlace: TaskStorePlaceInfo): InventoryTask {
        storePlace.isProcessed = false
        taskRepository.getProducts().getProcessedProducts(storePlace.placeCode).forEach {
            taskRepository.getProducts().changeProduct(it.copy(factCount = 0.0, isPositionCalc = false))
            taskRepository.getExciseStamps().deleteExciseStampsForProduct(it)
        }
        return this
    }

    fun processStorePlace(storePlaceNumber: String): StorePlaceProcessing {
        return StorePlaceProcessing(this, storePlaceNumber)
    }

    fun updateTaskWithContents(taskContents: TaskContents) {
        updateStorePlaces(taskContents.storePlaces)
        updateProducts(taskContents.products)
        updateExciseStamps(taskContents.exciseStamps)
    }

    fun hasDiscrepancies(): Boolean {
        return getDiscrepancies().isNotEmpty()
    }

    fun getDiscrepancies(): List<TaskProductInfo> {
        if (taskDescription.recountType == RecountType.ParallelByStorePlaces) {
            val processedProducts = taskRepository.getProducts().getProcessedProducts()
            return taskRepository.getProducts().getNotProcessedProducts().filter { productInfo ->
                processedProducts.findLast { it.materialNumber == productInfo.materialNumber && it.placeCode != "00" } == null
            }
        } else {
            return taskRepository.getProducts().getNotProcessedProducts()
        }
    }
}