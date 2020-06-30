package com.lenta.movement.requests.network.models.obtainingTaskComposition

import com.google.gson.annotations.SerializedName

/**"ET_TASK_PARTS"*/
data class TaskBatchInfoRestData(
        /** SAP-код товара */
        @SerializedName("MATNR")
        val materialNumber: String,

        /** Производитель товара */
        @SerializedName("ZPROD")
        var organizationCodeEGAIS: String,

        /** Дата производства */
        @SerializedName("DATEOFPOUR")
        val bottlingDate: String,

        /** Номер корзины */
        @SerializedName("FACT_QNT")
        val factQuantity: String,

        /** Номер корзины */
        @SerializedName("ZCHARG")
        val batchNumber: String,

        /** Номер корзины */
        @SerializedName("BASKET_NUM")
        val basketNumber: String
)