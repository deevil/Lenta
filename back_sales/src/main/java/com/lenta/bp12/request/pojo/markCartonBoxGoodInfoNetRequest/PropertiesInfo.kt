package com.lenta.bp12.request.pojo.markCartonBoxGoodInfoNetRequest

import com.google.gson.annotations.SerializedName

data class PropertiesInfo(
        /** ШК товара */
        @SerializedName("EAN")
        val ean: String,
        /** Наименование свойства */
        @SerializedName("NAME")
        val propertyName: String,
        /** Значение свойства */
        @SerializedName("VALUE")
        val propertyValue: String
)