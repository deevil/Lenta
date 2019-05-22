package com.lenta.bp10.mobileObjectsService.models

import com.google.gson.annotations.SerializedName

// модель значений параметров настройки
class MB_S_14(
        /// Название параметра
        @SerializedName("PARAMNAME")
        val paramname: String,

        /// Значение параметра
        @SerializedName("PARAMVALUE")
        val paramvalue: String
)