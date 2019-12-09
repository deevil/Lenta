package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName

//ET_TASK_PARTS Таблица партий задания
data class TaskBatchInfo(
        val materialNumber: String,
        val exidv: String, //Номер ЕО
        val batchNumber: String, //Номер партии
        val alcocode: String, //Номер товара в ЕГАИС (АлкоКод)
        val egais: String, //ЕГАИС Код организации
        val bottlingDate: String, //УТЗ ТСД: Дата розлива
        val purchaseOrderScope: Double, //Объем заказа на поставку
        val materialNumberSet: String // Номер набора
) {
    companion object {
        fun from(restData: TaskBatchInfoRestData): TaskBatchInfo {
            return TaskBatchInfo(
                    materialNumber = restData.materialNumber,
                    exidv = restData.exidv,
                    batchNumber = restData.batchNumber,
                    alcocode = restData.alcocode,
                    egais = restData.egais,
                    bottlingDate = restData.bottlingDate,
                    purchaseOrderScope = restData.purchaseOrderScope.toDouble(),
                    materialNumberSet = restData.materialNumberSet
                )
        }
    }

    fun getMaterialLastSix(): String {
        return if (materialNumber.length > 6)
            materialNumber.substring(materialNumber.length - 6)
        else
            materialNumber
    }
}

data class TaskBatchInfoRestData(
        @SerializedName("MATNR")
        val materialNumber: String,
        @SerializedName("EXIDV")
        val exidv: String,
        @SerializedName("ZCHARG")
        val batchNumber: String,
        @SerializedName("ZALCOCOD")
        val alcocode: String,
        @SerializedName("ZPROD")
        var egais: String,
        @SerializedName("DATEOFPOUR")
        val bottlingDate: String,
        @SerializedName("MENGE")
        val purchaseOrderScope: String,
        @SerializedName("MATNR_OSN")
        val materialNumberSet: String
) {

    companion object {
        fun from(data: TaskBatchInfo): TaskBatchInfoRestData {
            return TaskBatchInfoRestData(
                    materialNumber = data.materialNumber,
                    exidv = data.exidv,
                    batchNumber = data.batchNumber,
                    alcocode = data.alcocode,
                    egais = data.egais,
                    bottlingDate = data.bottlingDate,
                    purchaseOrderScope = data.purchaseOrderScope.toString(),
                    materialNumberSet = data.materialNumberSet
            )
        }
    }
}