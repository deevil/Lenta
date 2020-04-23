package com.lenta.bp16.request.pojo

import com.google.gson.annotations.SerializedName

data class PackInfo(
        /** SAP – код товара */
        @SerializedName("MATNR")
        val material: String,
        /** SAP – код товара */
        @SerializedName("MATNR_OSN")
        val materialOsn: String,
        /** SAP код сырья из которого был произведен брак */
        @SerializedName("MATNR_DEF")
        val materialDef: String,
        /** Номер технологического заказа */
        @SerializedName("AUFNR")
        val order: String,
        /** Код тары */
        @SerializedName("CODE_CONT")
        val code: String,
        /** Фактически вес ПФ/ГП */
        @SerializedName("FACT_QNT")
        val quantity: Double,
        /** Признак – «Дефростирование завершено» */
        @SerializedName("DEF_OUT")
        val isDefOut: String,
        /** Тип брака (категория) */
        @SerializedName("TYPE_DEF")
        val categoryCode: String,
        /** Код причины брака */
        @SerializedName("GRUND")
        val defectCode: String
)