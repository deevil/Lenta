package com.lenta.inventory.models.task

import com.lenta.inventory.models.repositories.ITaskRepository
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

    //возвращает модель сохранения задания
    fun getTaskSaveModel(): TaskSaveModel {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
        taskRepository.getProducts().getProcessedProducts(storePlace.placeCode).map {
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
}