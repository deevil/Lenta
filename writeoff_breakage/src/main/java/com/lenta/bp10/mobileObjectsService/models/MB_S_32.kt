package com.lenta.bp10.mobileObjectsService.models

import com.google.gson.annotations.SerializedName

// Модель справочника причин товарных движений для типов заданий (WOB)
class MB_S_32(
        /// Тип задания на списание
        @SerializedName("TASK_TYPE")
        val taskType: String,

        /// Тип контроля задания на списание
        @SerializedName("TASK_CNTRL")
        val taskCntrl: String,

        /// Причина движения
        @SerializedName("REASON")
        val reason: String,

        /// Текст к причине движения товара
        @SerializedName("GRTXT")
        val grtxt: String
)