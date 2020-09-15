package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName

//ET_ZPRODUCERS Таблица данных производителей для Z-партий
data class TaskManufacturersForZBatches (
        val materialNumber: String,
        val productBarcode: String ,
        val manufactureCode: String,
        val manufactureName: String
) {
        companion object {
                fun from(restData: TaskManufacturersForZBatchesRestData): TaskManufacturersForZBatches {
                        return TaskManufacturersForZBatches(
                                materialNumber = restData.materialNumber.orEmpty(),
                                productBarcode = restData.productBarcode.orEmpty(),
                                manufactureCode = restData.manufactureCode.orEmpty(),
                                manufactureName = restData.manufactureName.orEmpty()
                        )
                }
        }
}

data class TaskManufacturersForZBatchesRestData (
        @SerializedName("MATNR")
        val materialNumber: String?,
        @SerializedName("EAN")
        val productBarcode: String?,
        @SerializedName("GCP")
        val manufactureCode: String?,
        @SerializedName("NAME")
        val manufactureName: String?
) {
}