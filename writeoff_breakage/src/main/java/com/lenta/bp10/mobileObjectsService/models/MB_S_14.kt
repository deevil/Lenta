package com.lenta.bp10.mobileObjectsService.models

import com.fasterxml.jackson.annotation.JsonProperty

// модель значений параметров настройки
class MB_S_14(
        /// Название параметра
        @JsonProperty("PARAMNAME")
        val paramname: String,

        /// Значение параметра
        @JsonProperty("PARAMVALUE")
        val paramvalue: String
)