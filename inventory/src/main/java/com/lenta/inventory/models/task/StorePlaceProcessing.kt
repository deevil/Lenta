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

    //TODO предварительная версия. Логика будет уточнятся дополнятся
    fun getReport(isFinish: Boolean): InventoryReport {
        val taskDescription = inventoryTask.taskDescription
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
        return inventoryTask.taskRepository.getExciseStamps()
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
        return inventoryTask
                .taskRepository
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
        return (getNotProcessedProducts()).map {
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

}