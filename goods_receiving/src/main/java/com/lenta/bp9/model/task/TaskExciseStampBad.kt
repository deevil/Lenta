package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName

//ET_MARK_BAD Таблица плохих марок задания
data class TaskExciseStampBad(
        val processingUnitNumber: String,
        val materialNumber: String,
        val exciseStampCode: String,
        val boxNumber: String,
        val typeDiscrepancies: String,
        var isScan: Boolean )
{

    companion object {
        fun from(restData: TaskExciseStampBadRestData): TaskExciseStampBad {
            return TaskExciseStampBad(
                    processingUnitNumber = restData.processingUnitNumber,
                    materialNumber = restData.materialNumber,
                    exciseStampCode = restData.exciseStampCode,
                    boxNumber = restData.boxNumber,
                    typeDiscrepancies = restData.typeDiscrepancies,
                    isScan = restData.isScan.isNotEmpty()
            )
        }
    }
}

data class TaskExciseStampBadRestData(
        @SerializedName("EXIDV") //Номер ЕО
        val processingUnitNumber: String,
        @SerializedName("MATNR") //Номер товара
        val materialNumber: String,
        @SerializedName("MARK_NUM") //Код акцизной марки
        val exciseStampCode: String,
        @SerializedName("BOX_NUM") //Номер коробки
        val boxNumber: String,
        @SerializedName("GRUND") //Тип расхождения
        var typeDiscrepancies: String,
        @SerializedName("IS_SCAN")
        var isScan: String
) {

    companion object {
        fun from(data: TaskExciseStampBad): TaskExciseStampBadRestData {
            return TaskExciseStampBadRestData(
                    processingUnitNumber = data.processingUnitNumber,
                    materialNumber = data.materialNumber,
                    exciseStampCode = data.exciseStampCode,
                    boxNumber = data.boxNumber,
                    typeDiscrepancies = data.typeDiscrepancies,
                    isScan = if (data.isScan) "X" else ""
            )
        }
    }
}