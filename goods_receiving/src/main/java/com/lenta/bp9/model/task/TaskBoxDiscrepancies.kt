package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName

//ET_BOX_DIFF Таблица обработанных коробов
data class TaskBoxDiscrepancies(
        val processingUnitNumber: String,
        val materialNumber: String,
        val boxNumber: String,
        val typeDiscrepancies: String,
        var isScan: Boolean )
{

    companion object {
        fun from(restData: TaskBoxDiscrepanciesRestData): TaskBoxDiscrepancies {
            return TaskBoxDiscrepancies(
                    processingUnitNumber = restData.processingUnitNumber,
                    materialNumber = restData.materialNumber,
                    boxNumber = restData.boxNumber,
                    typeDiscrepancies = restData.typeDiscrepancies,
                    isScan = restData.isScan.isNotEmpty()
            )
        }
    }
}

data class TaskBoxDiscrepanciesRestData(
        @SerializedName("EXIDV") //Номер ЕО
        val processingUnitNumber: String,
        @SerializedName("MATNR") //Номер товара
        val materialNumber: String,
        @SerializedName("BOX_NUM") //Номер коробки
        val boxNumber: String,
        @SerializedName("GRUND") //Тип расхождения
        var typeDiscrepancies: String,
        @SerializedName("IS_SCAN")
        var isScan: String
) {

    companion object {
        fun from(data: TaskBoxDiscrepancies): TaskBoxDiscrepanciesRestData {
            return TaskBoxDiscrepanciesRestData(
                    processingUnitNumber = data.processingUnitNumber,
                    materialNumber = data.materialNumber,
                    boxNumber = data.boxNumber,
                    typeDiscrepancies = data.typeDiscrepancies,
                    isScan = if (data.isScan) "X" else ""
            )
        }
    }
}