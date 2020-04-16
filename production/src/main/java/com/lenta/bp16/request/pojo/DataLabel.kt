package com.lenta.bp16.request.pojo

import com.google.gson.annotations.SerializedName

data class DataLabel(
        /** SAP код сырья (из позиции новой тары) */
        @SerializedName("MATNR")
        val material: String,
        /** Наименование товара сырья */
        @SerializedName("NAME_MATNR")
        val materialName: String,
        /** Наименование готового продукта */
        @SerializedName("NAME_MATNR_DONE")
        val materialNameDone: String,
        /** Условия хранения + срок годности в часах */
        @SerializedName("STOR_COND_TIME")
        val storCondTime: String,
        /** Срок годности */
        @SerializedName("ZMD_REAL_TIME")
        val time: String,
        /** Тип срок годности (часы или дни) */
        @SerializedName("ZMD_REAL_PER")
        val timeType: String,
        /** Плановое время окончания этапа (из технологического заказа в часах) */
        @SerializedName("PLAN_AUF_FINISH")
        val planAufFinish: String,
        /** Неизвестно... */
        @SerializedName("PLAN_AUF_UNIT")
        val planAufUnit: String,
        /** Штрих код товара (из MEAN) */
        @SerializedName("EAN")
        val ean: String
)