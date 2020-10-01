package com.lenta.bp15.repository.net_requests.pojo

import com.google.gson.annotations.SerializedName

data class PositionRawInfo(
        /** SAP-код товара */
        @SerializedName("MATNR")
        val material: String,
        /** Плановое количество марок к оклейке */
        @SerializedName("PLAN_QNT")
        val planQuantity: String,
        /** Тип марки */
        @SerializedName("MARK_TYPE")
        val markTypeCode: String,
        /** Признак «Товар отечественный» */
        @SerializedName("IS_RUS")
        val isRus: String
)