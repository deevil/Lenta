package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName

//ET_TASK_ZPARTS Таблица Z-партий задания
data class TaskZBatchInfo(
        val processingUnit: String, //Номер ЕО
        val materialNumber: String,
        val batchNumber: String, //Номер партии
        val manufactureCode: String, //ЕГАИС Код организации
        val productionDate: String, //Дата производства
        val productionTime: String, //Время  производства
        val shelfLifeDate: String, //Срок годности до (Дата)
        val shelfLifeTime: String, //Срок годности до (время)
        val purchaseOrderScope: Double, //Объем заказа на поставку
        val alternativeUnitMeasure: String, // альтернативная единица измерения
        val quantityAlternativeUnitMeasure: Double // количество в альтернативной единице измерения
) {
    companion object {
        fun from(restData: TaskZBatchInfoRestData): TaskZBatchInfo {
            return TaskZBatchInfo(
                    processingUnit = restData.processingUnit.orEmpty(),
                    materialNumber = restData.materialNumber.orEmpty(),
                    batchNumber = restData.batchNumber.orEmpty(),
                    manufactureCode = restData.manufactureCode.orEmpty(),
                    productionDate = restData.productionDate.orEmpty(),
                    productionTime = restData.productionTime.orEmpty(),
                    shelfLifeDate = restData.shelfLifeDate.orEmpty(),
                    shelfLifeTime = restData.shelfLifeTime.orEmpty(),
                    purchaseOrderScope = restData.purchaseOrderScope?.toDoubleOrNull() ?: 0.0,
                    alternativeUnitMeasure = restData.alternativeUnitMeasure.orEmpty(),
                    quantityAlternativeUnitMeasure = restData.quantityAlternativeUnitMeasure?.toDoubleOrNull() ?: 0.0
            )
        }
    }
}

data class TaskZBatchInfoRestData(
        @SerializedName("EXIDV")
        val processingUnit: String?,
        @SerializedName("MATNR")
        val materialNumber: String?,
        @SerializedName("BATCH")
        val batchNumber: String?,
        @SerializedName("PRODUCER")
        var manufactureCode: String?,
        @SerializedName("PROD_DATE")
        val productionDate: String?,
        @SerializedName("PROD_TIME")
        val productionTime: String?,
        @SerializedName("SHELF_LIFE_D")
        val shelfLifeDate: String?,
        @SerializedName("SHELF_LIFE_T")
        val shelfLifeTime: String?,
        @SerializedName("MENGE")
        val purchaseOrderScope: String?,
        @SerializedName("ALTME")
        val alternativeUnitMeasure: String?, // альтернативная единица измерения
        @SerializedName("ALTVOLUME")
        val quantityAlternativeUnitMeasure: String? // количество в альтернативной единице измерения
) {
}