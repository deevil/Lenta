package com.lenta.bp16.model.movement.params

import com.google.gson.annotations.SerializedName

data class MovementParams(
        /**Номер ТК*/
        @SerializedName("IV_WERKS")
        val tkNumber: String,
        /**Товар*/
        @SerializedName("IV_MATNR")
        val matnr: String,
        /**Код производителя*/
        @SerializedName("IV_PROD_CODE")
        val prodCode: String,
        /**Дата производства*/
        @SerializedName("IV_DATE_PROD")
        val dateProd: String,
        /**Срок годности*/
        @SerializedName("IV_EXPIR_DATE")
        val expirDate: String,
        /**Склад отправитель*/
        @SerializedName("IV_LGORT_EXPORT")
        val lgortExport: String,
        /**Склад получатель*/
        @SerializedName("IV_LGORT_IMPORT")
        val lgortImport: String,
        /**Код тары*/
        @SerializedName("IV_CODE_CONT")
        val codeCont: String,
        /**Количество*/
        @SerializedName("IV_FACT_QNT")
        val factQnt: String,
        /**Единицы измерения*/
        @SerializedName("IV_BUOM")
        val buom: String,
        /**IP устройства*/
        @SerializedName("IV_IP_PDA")
        val deviceIP: String,
        /**Табельный номер*/
        @SerializedName("IV_PERNR")
        val personnelNumber: String
)