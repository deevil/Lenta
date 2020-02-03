package com.lenta.bp16.request.pojo

import com.google.gson.annotations.SerializedName

data class DataLabel(
        /** Какие */
        @SerializedName("MATNR")
        val material: String,
        /** Какие */
        @SerializedName("NAME_MATNR")
        val materialName: String,
        /** Неизвестно... */
        @SerializedName("NAME_MATNR_DONE")
        val materialNameDone: String,
        /** Неизвестно... */
        @SerializedName("STOR_COND_TIME")
        val storCondTime: String,
        /** Срок годности */
        @SerializedName("DATE_EXPIR")
        val dateExpiration: String,
        /** Неизвестно... */
        @SerializedName("PLAN_AUF_FINISH")
        val planAufFinish: String,
        /** Неизвестно... */
        @SerializedName("PLAN_AUF_UNIT")
        val planAufUnit: String,
        /** Штрих-код */
        @SerializedName("EAN")
        val ean: String

)