package com.lenta.bp14.requests.pojo

import com.google.gson.annotations.SerializedName

data class MarkStatus(
        /** Статус марки: 1 - корректна, 2 - проблемная, 3 - принадлежит другому товару */
        @SerializedName("EV_STAT")
        val status: String,
        /** Описание для 2 и 3 статусов */
        @SerializedName("EV_STAT_TEXT")
        val description: String
)