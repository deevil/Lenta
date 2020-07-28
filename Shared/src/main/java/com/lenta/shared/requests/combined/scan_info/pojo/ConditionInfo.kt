package com.lenta.shared.requests.combined.scan_info.pojo

import com.google.gson.annotations.SerializedName

data class ConditionInfo(
        /**Код предприятия*/
        @SerializedName("WERKS")
        val werks: String,
        /**Товар*/
        @SerializedName("MATNR")
        val matnr: String,
        /**Код условия хранения*/
        @SerializedName("STCOND")
        val number: String,
        /**Наименование условия хранения*/
        @SerializedName("STCONDNAM")
        val name: String,
        /**Условия хранения по умолчанию*/
        @SerializedName("DEFCOND")
        val defCondition: String
)