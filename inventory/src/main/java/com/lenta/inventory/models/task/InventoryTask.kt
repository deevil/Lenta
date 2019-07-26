package com.lenta.inventory.models.task

import com.lenta.inventory.models.repositories.ITaskRepository
import com.lenta.inventory.requests.network.ExciseStampInfo
import com.lenta.inventory.requests.network.InventoryReport
import com.lenta.inventory.requests.network.MaterialNumber
import com.lenta.inventory.requests.network.StorePlace
import com.lenta.shared.models.core.ProductType

class InventoryTask(val taskDescription: TaskDescription, val taskRepository: ITaskRepository) {

    //фун-ция проверят, является ли продукт обычным либо безакцизным алкоголем
    fun processGeneralProduct(product: TaskProductInfo): ProcessGeneralProductService? {
        return if (product.type == ProductType.General || product.type == ProductType.NonExciseAlcohol) {
            ProcessGeneralProductService(taskDescription, taskRepository, product)
        } else null
    }

    //фун-ция проверят, является ли продукт акцизным алкоголем
    fun processExciseAlcoProduct(product: TaskProductInfo): ProcessExciseAlcoProductService? {
        return if (product.type == ProductType.ExciseAlcohol) {
            ProcessExciseAlcoProductService(taskDescription, taskRepository, product)
        } else null
    }

    fun getProcessedStorePlaces(): List<TaskStorePlaceInfo> {
        return taskRepository.getStorePlace().getStorePlaces().filter { it.isProcessed }
    }

    fun getUnprocessedStorePlaces(): List<TaskStorePlaceInfo> {
        return taskRepository.getStorePlace().getStorePlaces().filter { !it.isProcessed }
    }

    fun getProductsQuantityForStorePlace(storePlaceNumber: String): Int {
        return taskRepository.getProducts().getProducts().count { it.placeCode == storePlaceNumber }
    }

    //Вызывается в двух случаях:
    //1. Нестрогий список, обычный пересчет, товар по умолчанию находится в общем МХ 00
    //2. Строгий список, пересчет по МХ, по нажатию "Отвязать", удаляем товар в конкретном заданном МХ
    fun deleteProduct(productNumber: String, storePlaceNumber: String = "00")
    {
        taskRepository.getProducts().findProduct(productNumber, storePlaceNumber)?.let {
            taskRepository.getProducts().deleteProduct(it)
        }
    }

    fun untieProduct(productNumber: String, storePlaceNumber: String) {
        deleteProduct(productNumber, storePlaceNumber)
        //TODO: сохранять список отвязанных товаров для передачи в сохранение
    }


    //Если не передавать номер МХ, то отсутствующими помечаются товары с заданным номером во всех МХ
    fun markProductMissing(productNumber: String, storePlaceNumber: String? = null) {
        if (storePlaceNumber != null) {
            taskRepository.getProducts().findProduct(productNumber, storePlaceNumber)?.let {
                it.isPositionCalc = true
                it.factCount = 0.0
            }
        } else {
            taskRepository.getProducts().getProducts().filter {
                it.materialNumber == productNumber
            }.forEach {
                it.factCount = 0.0
                it.isPositionCalc = true
            }
        }
    }

    //TODO предварительная версия. Логика будет уточнятся дополнятся
    fun getReport(isFinish: Boolean): InventoryReport {
        val taskDescription = taskDescription
        return InventoryReport(
                tkNumber = taskDescription.stock,
                ipAdress = taskDescription.lockIP,
                taskNumber = taskDescription.taskNumber,
                isFinish = if (isFinish) "X" else "",
                personnelNumber = taskDescription.lockUser,
                storePlacesForDelete = getReportsStorePlaces(),
                products = getReportsProducts(),
                stamps = getReportStamps(),
                isRecount = ""
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
    private fun getReportsStorePlaces(): List<StorePlace> {
        return taskRepository
                .getStorePlace().getStorePlaces().map {
                    StorePlace(
                            storage = "",
                            placeCode = it.placeCode,
                            matNumber = "",
                            isDel = ""
                    )
                }
    }

    private fun getReportsProducts(): List<MaterialNumber> {
        return (taskRepository.getProducts().getNotProcessedProducts()).map {
            MaterialNumber(
                    materialNumber = it.materialNumber,
                    storePlaceCode = it.placeCode,
                    factQuantity = it.factCount.toString(),
                    positionCounted = if (it.isPositionCalc) "X" else "",
                    isDel = if (it.factCount > 0.0) "" else "X",
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
            it.isPositionCalc = false
            it.factCount = 0.0
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

    fun hasDiscrepancies() : Boolean {
        return getDiscrepancies().isNotEmpty()
    }

    fun getDiscrepancies() : List<TaskProductInfo> {
        return taskRepository.getProducts().getNotProcessedProducts()
    }
}