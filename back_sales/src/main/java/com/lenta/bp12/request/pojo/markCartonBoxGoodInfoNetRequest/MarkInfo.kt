package com.lenta.bp12.request.pojo.markCartonBoxGoodInfoNetRequest

import com.google.gson.annotations.SerializedName

/** Список марок задания для передачи в МП */
data class MarkInfo (
        @SerializedName("MATNR")
        val goodNumber: String?,
        @SerializedName("BOX_NUM")
        val boxNumber: String?,
        @SerializedName("MARK_NUM")
        val markNumber: String?,
        @SerializedName("PACK_NUM")
        val cartonNumber: String?
)