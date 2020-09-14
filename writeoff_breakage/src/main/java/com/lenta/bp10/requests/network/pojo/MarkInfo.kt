package com.lenta.bp10.requests.network.pojo

import com.google.gson.annotations.SerializedName

data class MarkInfo(
        /** SAP-код товара */
        @SerializedName("MATNR")
        var material: String?,
        /** Номер коробки */
        @SerializedName("BOX_NUM")
        var boxNumber: String?,
        /** Код акцизной марки */
        @SerializedName("MARK_NUM")
        var markNumber: String?,
        /** Номер дополнительной упаковки */
        @SerializedName("PACK_NUM")
        var packNumber: String?
)