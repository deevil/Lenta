package com.lenta.bp12.request.pojo.good_info

import com.google.gson.annotations.SerializedName

/**
 * Параметры для получения данных товара по ШК\SAP-коду
 * "ZMP_UTZ_BKS_05_V001"
 * @see com.lenta.bp12.request.GoodInfoNetRequest
 */
data class GoodInfoParams(
        /** Номер ТК */
        @SerializedName("IV_WERKS")
        val tkNumber: String,
        /** ШК товара */
        @SerializedName("IV_EAN")
        val ean: String = "",
        /** SAP-код товара */
        @SerializedName("IV_MATNR")
        val material: String = "",
        /** Код бизнес процесса */
        @SerializedName("IV_CODEBP")
        val bpCode: String = "BKS",
        /** Код бизнес процесса */
        @SerializedName("IV_TYPE_TASK")
        val taskType: String,
        /** Индикатор из одной позиции */
        @SerializedName("IV_MODE")
        val mode: String = ""
)