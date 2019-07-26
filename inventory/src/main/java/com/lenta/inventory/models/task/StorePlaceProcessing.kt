package com.lenta.inventory.models.task

import com.lenta.inventory.requests.network.ExciseStampInfo
import com.lenta.inventory.requests.network.InventoryReport
import com.lenta.inventory.requests.network.MaterialNumber
import com.lenta.inventory.requests.network.StorePlace

class StorePlaceProcessing(val inventoryTask: InventoryTask, val storePlaceNumber: String) {

    fun markAsProcessed() {
        val storePlace = inventoryTask.taskRepository.getStorePlace().findStorePlace(storePlaceNumber)
        storePlace?.let {
            it.isProcessed = true
        }
    }

    //фун-ция возвращает НЕ ОБРАБОТАННЫЕ товары
    fun getNotProcessedProducts(): List<TaskProductInfo> {
        return inventoryTask.taskRepository.getProducts().getNotProcessedProducts(storePlaceNumber)
    }

    //фун-ция возвращает ОБРАБОТАННЫЕ товары
    fun getProcessedProducts(): List<TaskProductInfo> {
        return inventoryTask.taskRepository.getProducts().getProcessedProducts(storePlaceNumber)
    }

    //TODO добавляет новый продукт в задание, есть в REST-96
    fun addNewProduct(product: TaskProductInfo): StorePlaceProcessing {
        return StorePlaceProcessing(inventoryTask, storePlaceNumber)
    }

    fun getTotalCountOfProduct(product: TaskProductInfo): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun clearProduct(product: TaskProductInfo): StorePlaceProcessing {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun deleteProduct(product: TaskProductInfo): StorePlaceProcessing {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun clearExciseStamps(product: TaskProductInfo): StorePlaceProcessing {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}