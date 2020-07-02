package com.lenta.movement.requests.network.models.saveTask

import com.google.gson.annotations.SerializedName

/** IT_TASK_POS Таблица состава задания*/
data class SaveTaskParamsTaskMaterial(
        @SerializedName("MATNR")
        val number: String,
        @SerializedName("FACT_QNT")
        val quantity: String,

        /**Индикатор: Позиция посчитана*/
        @SerializedName("XZAEL")
        val positionCounted: String,

        @SerializedName("IS_DEL")
        val isDeleted: String,
        @SerializedName("MEINS")
        val uom: String
)