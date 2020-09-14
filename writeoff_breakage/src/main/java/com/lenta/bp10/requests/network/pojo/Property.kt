package com.lenta.bp10.requests.network.pojo

import com.google.gson.annotations.SerializedName

data class Property(
        /** Наименование свойства */
        @SerializedName("NAME")
        val name: String,
        /** Значение свойства */
        @SerializedName("VALUE")
        val value: String
)