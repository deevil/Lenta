package com.lenta.bp12.model.pojo

import com.google.gson.annotations.SerializedName

data class ProdItem(
        /** SAP-код товара */
        @SerializedName("MATNR")
        var material: String,
        /** Производитель товара */
        @SerializedName("ZPROD")
        var producer: String
)