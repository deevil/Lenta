package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName

//ET_TASK_PACK	Список блоков для маркированного товара
data class TaskBlockInfo(val processingUnitNumber: String,
                       val materialNumber: String,
                       val blockNumber: String,
                       val boxNumber: String )
{

    companion object {
        fun from(restData: TaskBlockInfoRestData): TaskBlockInfo {
            return TaskBlockInfo(
                    processingUnitNumber = restData.processingUnitNumber,
                    materialNumber = restData.materialNumber,
                    blockNumber = restData.blockNumber,
                    boxNumber = restData.boxNumber
            )
        }
    }
}

data class TaskBlockInfoRestData(
        @SerializedName("EXIDV") //Номер ЕО
        val processingUnitNumber: String,
        @SerializedName("MATNR") //Номер товара
        val materialNumber: String,
        @SerializedName("PACK_NUM") //Номер блока
        val blockNumber: String,
        @SerializedName("BOX_NUM") //Номер коробки
        val boxNumber: String
) {

    companion object {
        fun from(data: TaskBlockInfo): TaskBlockInfoRestData {
            return TaskBlockInfoRestData(
                    processingUnitNumber = data.processingUnitNumber,
                    materialNumber = data.materialNumber,
                    blockNumber = data.blockNumber,
                    boxNumber = data.boxNumber
            )
        }
    }
}