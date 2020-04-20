package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName

data class TaskSearchParams(
        /** Код поставщика */
        @SerializedName("LIFNR")
        var providerCode: String,
        /** SAP-код товара */
        @SerializedName("MATNR")
        var material: String,
        /** Номер отдела (секция) */
        @SerializedName("ABTNR")
        var section: String,
        /** Код акцизной марки */
        @SerializedName("MARK_NUM")
        var exciseMark: String
)