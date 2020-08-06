package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName

//ET_PACK_DIFF Таблица обработанных блоков (маркированный товар)
data class TaskBlockDiscrepancies(
        val processingUnitNumber: String,
        val materialNumber: String,
        val blockNumber: String,
        val boxNumber: String,
        val typeDiscrepancies: String,
        val isScan: Boolean,
        val isMsc: Boolean,
        val isUnknown: Boolean,
        val isGrayZone: Boolean)
{

    companion object {
        fun from(restData: TaskBlockDiscrepanciesRestData): TaskBlockDiscrepancies {
            return TaskBlockDiscrepancies(
                    processingUnitNumber = restData.processingUnitNumber,
                    materialNumber = restData.materialNumber,
                    blockNumber = restData.blockNumber,
                    boxNumber = restData.boxNumber,
                    typeDiscrepancies = restData.typeDiscrepancies,
                    isScan = restData.isScan.isNotEmpty(),
                    isMsc = restData.isMsc.isNotEmpty(),
                    isUnknown = restData.isUnknown.isNotEmpty(),
                    isGrayZone = restData.isGrayZone?.isNotEmpty() ?: false
            )
        }
    }
}

data class TaskBlockDiscrepanciesRestData(
        @SerializedName("EXIDV") //Номер ЕО
        val processingUnitNumber: String,
        @SerializedName("MATNR") //Номер товара
        val materialNumber: String,
        @SerializedName("PACK_NUM") //Номер блока
        val blockNumber: String,
        @SerializedName("BOX_NUM") //Номер коробки
        val boxNumber: String,
        @SerializedName("GRUND") //Тип расхождения
        val typeDiscrepancies: String,
        @SerializedName("IS_SCAN")
        val isScan: String,
        @SerializedName("IS_MSC")
        val isMsc: String,
        @SerializedName("IS_UNKNOWN")
        val isUnknown: String,
        @SerializedName("IS_GRAYZONE")
        val isGrayZone: String?
) {

    companion object {
        fun from(data: TaskBlockDiscrepancies): TaskBlockDiscrepanciesRestData {
            return TaskBlockDiscrepanciesRestData(
                    processingUnitNumber = data.processingUnitNumber,
                    materialNumber = data.materialNumber,
                    blockNumber = data.blockNumber,
                    boxNumber = data.boxNumber,
                    typeDiscrepancies = data.typeDiscrepancies,
                    isScan = if (data.isScan) "X" else "",
                    isMsc = if (data.isMsc) "X" else "",
                    isUnknown = if (data.isUnknown) "X" else "",
                    isGrayZone = if (data.isGrayZone) "X" else ""
            )
        }
    }
}