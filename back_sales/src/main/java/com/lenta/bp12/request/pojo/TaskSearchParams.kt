package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName

data class TaskSearchParams(
        /** Код поставщика */
        @SerializedName("LIFNR")
        var supplierCode: String,
        /** SAP-код товара */
        @SerializedName("MATNR")
        var material: String,
        /** Номер отдела (секция) */
        @SerializedName("ABTNR")
        var sectionNumber: String,
        /** Код акцизной марки */
        @SerializedName("MARK_NUM")
        var exciseMarkCode: String
)