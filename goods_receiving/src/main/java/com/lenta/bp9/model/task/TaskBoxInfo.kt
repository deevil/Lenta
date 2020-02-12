package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName

//ET_TASK_BOX	Список коробок задания для передачи в МП
data class TaskBoxInfo(val processingUnitNumber: String,
                       val materialNumber: String,
                       val boxNumber: String )
{

    companion object {
        fun from(restData: TaskBoxInfoRestData): TaskBoxInfo {
            return TaskBoxInfo(
                    processingUnitNumber = restData.processingUnitNumber,
                    materialNumber = restData.materialNumber,
                    boxNumber = restData.boxNumber
            )
        }
    }
}

data class TaskBoxInfoRestData(
        @SerializedName("EXIDV") //Номер ЕО
        val processingUnitNumber: String,
        @SerializedName("MATNR") //Номер товара
        val materialNumber: String,
        @SerializedName("BOX_NUM") //Номер коробки
        val boxNumber: String
) {

    companion object {
        fun from(data: TaskBoxInfo): TaskBoxInfoRestData {
            return TaskBoxInfoRestData(
                    processingUnitNumber = data.processingUnitNumber,
                    materialNumber = data.materialNumber,
                    boxNumber = data.boxNumber
            )
        }
    }
}