package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName

//IT_BOX_DIFF Таблица обработанных коробов
data class TaskProcessedBoxInfo(
        val processingUnitNumber: String,
        val materialNumber: String,
        val boxNumber: String,
        val reasonRejection: String,
        var isScan: Boolean )
{

    companion object {
        fun from(restData: TaskProcessedBoxInfoRestData): TaskProcessedBoxInfo {
            return TaskProcessedBoxInfo(
                    processingUnitNumber = restData.processingUnitNumber,
                    materialNumber = restData.materialNumber,
                    boxNumber = restData.boxNumber,
                    reasonRejection = restData.reasonRejection,
                    isScan = restData.isScan.isNotEmpty()
            )
        }
    }
}

data class TaskProcessedBoxInfoRestData(
        @SerializedName("EXIDV") //Номер ЕО
        val processingUnitNumber: String,
        @SerializedName("MATNR") //Номер товара
        val materialNumber: String,
        @SerializedName("BOX_NUM") //Номер коробки
        val boxNumber: String,
        @SerializedName("GRUND") //Причина отклонения при ППП
        var reasonRejection: String,
        @SerializedName("IS_SCAN")
        var isScan: String
) {

    companion object {
        fun from(data: TaskProcessedBoxInfo): TaskProcessedBoxInfoRestData {
            return TaskProcessedBoxInfoRestData(
                    processingUnitNumber = data.processingUnitNumber,
                    materialNumber = data.materialNumber,
                    boxNumber = data.boxNumber,
                    reasonRejection = data.reasonRejection,
                    isScan = if (data.isScan) "X" else ""
            )
        }
    }
}