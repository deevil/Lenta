package com.lenta.bp18.request.model

import com.google.gson.annotations.SerializedName

data class GoodInfoParams(
        /**Номер ТК;*/
        @SerializedName("IV_WERKS")
        val marketNumber: String,
        /**SAP-код товара*/
        @SerializedName("IV_MATNR")
        val sapCode: String,
        /**Код группы весового оборудования*/
        @SerializedName("IV_GRNUM")
        val grNum: String,
        /**Код условия хранения*/
        @SerializedName("IV_STCOND")
        val stdCond: String,
        /**Количество*/
        @SerializedName("IV_FACT_QNT")
        val quantity: String,
        /**Единица измерения*/
        @SerializedName("IV_BUOM")
        val buom: String,
        /**Номер партии*/
        @SerializedName("IV_NUM_PART")
        val partNumber: String,
        /**Уникальный идентификатор*/
        @SerializedName("IV_GUID")
        val guid: String,
        /**Дата вскрытия упаковки*/
        @SerializedName("IV_DATE_OPEN")
        val dateOpen: String,
        /**Время вскрытия упаковки*/
        @SerializedName("IV_TIME_OPEN")
        val timeOpen: String,
        /**Глобальный номер товара*/
        @SerializedName("IV_EAN")
        val ean: String
)