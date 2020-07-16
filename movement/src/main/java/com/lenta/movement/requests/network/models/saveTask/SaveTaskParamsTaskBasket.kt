package com.lenta.movement.requests.network.models.saveTask

import com.google.gson.annotations.SerializedName

data class SaveTaskParamsTaskBasket(
        @SerializedName("BASKET_NUM")
        val basketNumber: String,
        @SerializedName("MATNR")
        val materialNumber: String,
        @SerializedName("FACT_QNT")
        val quantity: String,

        /** Базисная единица измерения */
        @SerializedName("MEINS")
        val uom: String,

        @SerializedName("MTART")
        val materialType: String,
        @SerializedName("LIFNR")
        val lifNr: String,

        /** Номер партии */
        @SerializedName("ZCHARG")
        val zcharg: String,

        @SerializedName("IS_MARK_STOCKS")
        val isExcise: String,
        @SerializedName("IS_PARTS_STOCKS")
        val isNotExcise: String,
        @SerializedName("IS_ALCO")
        val isAlco: String,
        @SerializedName("IS_USUAL")
        val isUsual: String,
        @SerializedName("IS_VET")
        val isVet: String,
        @SerializedName("IS_FOOD")
        val isFood: String
)