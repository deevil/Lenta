package com.lenta.inventory.models.task

class StorePlaceProcessing(val inventoryTask: InventoryTask, val storePlaceNumber: String) {

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