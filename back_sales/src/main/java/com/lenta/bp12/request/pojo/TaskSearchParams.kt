package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName

/**
 * Параметры поиска списка заданий "ZMP_UTZ_BKS_02_V001"
 * @see com.lenta.bp12.request.TaskListNetRequest
 */
data class TaskSearchParams(
        /** Код поставщика */
        @SerializedName("LIFNR")
        val providerCode: String,
        /** SAP-код товара */
        @SerializedName("MATNR")
        val goodNumber: String,
        /** Номер отдела (секция) */
        @SerializedName("ABTNR")
        val section: String,
        /** Код акцизной марки */
        @SerializedName("MARK_NUM")
        val exciseNumber: String,
        /** Код клиента */
        @SerializedName("KUNNR")
        val clientCode: String
)