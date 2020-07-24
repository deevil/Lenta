package com.lenta.movement.requests.network.models.obtainingTaskComposition

import com.google.gson.annotations.SerializedName

/**"ET_TASK_MARK"*/
data class TaskExciseStampInfoRestData(
        /** SAP-код товара */
        @SerializedName("MATNR") //Номер товара
        val materialNumber: String,

        /** Поставщик */
        @SerializedName("LIFNR")
        val supplier: String,

        /** Код акцизной марки */
        @SerializedName("MARK_NUM")
        val code: String,

        /** Номер коробки */
        @SerializedName("BOX_NUM")
        val boxNumber: String,

        /** Проблемная марка */
        @SerializedName("IS_MARK_BAD")
        val isMarkBad: String,

        /** Номер корзины */
        @SerializedName("BASKET_NUM")
        val basketNumber: String
)