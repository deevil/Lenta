package com.lenta.inventory.models.task

import com.lenta.shared.models.core.ProductType

class StorePlaceProcessing(val inventoryTask: InventoryTask, val storePlaceNumber: String) {

    //TODO фун-ция проверят, является ли продукт обычным либо безакцизным алкоголем
    fun isGeneralProduct(product: TaskProductInfo): Boolean {
        return product.type == ProductType.General || product.type == ProductType.NonExciseAlcohol
    }

    //TODO фун-ция проверят, является ли продукт акцизным алкоголем
    fun isExciseAlcoProduct(product: TaskProductInfo): Boolean {
        return product.type == ProductType.ExciseAlcohol
    }

    //TODO фун-ция возвращает НЕ ОБРАБОТАННЫЕ товары
    fun getNotProcessedProducts(){
        inventoryTask.taskRepository.getProducts().getNotProcessedProducts()
    }

    //TODO фун-ция возвращает ОБРАБОТАННЫЕ товары
    fun getProcessedProducts(){
        inventoryTask.taskRepository.getProducts().getProcessedProducts()
    }

    fun processProduct(){
        //проваливаемся в продукт
    }

    //TODO добавляет новый продукт в задание, есть в REST-96
    fun addNewProduct( product: TaskProductInfo) : StorePlaceProcessing {
        return StorePlaceProcessing(inventoryTask, storePlaceNumber)
    }

    fun getTotalCountOfProduct(product: TaskProductInfo): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun clearProduct( product: TaskProductInfo) : StorePlaceProcessing {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun deleteProduct( product: TaskProductInfo) : StorePlaceProcessing {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun clearExciseStamps( product: TaskProductInfo) : StorePlaceProcessing {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}