package com.lenta.bp16.request.pojo

import com.google.gson.annotations.SerializedName

data class Product(
        /**Номер товара*/
        @SerializedName("MATNR")
        val matnr: String?
)