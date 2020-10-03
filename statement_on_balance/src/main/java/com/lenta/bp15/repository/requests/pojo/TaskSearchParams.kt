package com.lenta.bp15.repository.requests.pojo

import com.google.gson.annotations.SerializedName

data class TaskSearchParams(
        /** Товар */
        @SerializedName("MATNR")
        val material: String,
        /** Тип марки: M – Обувь мужская, F – Обувь женская, C – Обувь детская, O – Другое */
        @SerializedName("MARK_TYPE")
        val markType: String
)