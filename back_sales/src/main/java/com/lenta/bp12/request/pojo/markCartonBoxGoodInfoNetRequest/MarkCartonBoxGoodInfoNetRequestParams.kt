package com.lenta.bp12.request.pojo.markCartonBoxGoodInfoNetRequest

import com.google.gson.annotations.SerializedName

data class MarkCartonBoxGoodInfoNetRequestParams(
        /** Код бизнес процесса */
        @SerializedName("IV_CODEBP")
        val bpCode: String = "BKS",
        /** Номер задания */
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String = "",
        /** Номер короба */
        @SerializedName("IV_BOX_NUM")
        val boxNumber: String = "",
        /** Номер блока */
        @SerializedName("IV_PACK_NUM")
        val cartonNumber: String = "",
        /** Номер марки */
        @SerializedName("IV_MARK_NUM")
        val markNumber: String = "",
        /** Номер товара */
        @SerializedName("IV_MATNR") // Для BKS не используется
        val goodNumber: String = "",
        /** Тип маркировки */
        @SerializedName("IV_ZMARKTYPE")
        val markType: String
)