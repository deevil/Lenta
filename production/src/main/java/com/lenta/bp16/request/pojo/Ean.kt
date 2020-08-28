package com.lenta.bp16.request.pojo

import com.google.gson.annotations.SerializedName

data class Ean (
        /**Глобальный номер товара*/
        @SerializedName("EAN")
        val ean: String?
)