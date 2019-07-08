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

    //фун-ция возвращает НЕ ОБРАБОТАННЫЕ товары
    fun getNotProcessedProducts(){
        taskRepository.getProducts().getNotProcessedProducts()
    }

    //фун-ция возвращает ОБРАБОТАННЫЕ товары
    fun getProcessedProducts(){
        taskRepository.getProducts().getProcessedProducts()
    }

    //возвращает модель сохранения задания
    fun getTaskSaveModel(): TaskSaveModel {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //вызывается при возврате на 20 экран и при нажатии на кнопку ОБНОВИТЬ, вызываем 96 рест
    fun updateStorePlaces() : InventoryTask {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun clearStorePlace() : InventoryTask {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun processStorePlace(storePlaceNumber: String) : StorePlaceProcessing{
        return StorePlaceProcessing(this,  storePlaceNumber)
    }






}