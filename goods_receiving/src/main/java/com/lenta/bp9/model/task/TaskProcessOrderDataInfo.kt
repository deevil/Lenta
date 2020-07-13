package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName

class TaskProcessOrderDataInfo(
        val materialNumber: String, //Номер товара
        val processOrderData: String //Технологический заказ
) {

    companion object {
        fun from(restData: TaskProcessOrderDataRestData): TaskProcessOrderDataInfo {
            return TaskProcessOrderDataInfo(
                    materialNumber = restData.materialNumber,
                    processOrderData = restData.processOrderData
            )
        }
    }
}

data class TaskProcessOrderDataRestData(
        @SerializedName("MATNR")
        val materialNumber: String, //Номер товара
        @SerializedName("AUFNR")
        val processOrderData: String //Технологический заказ
)