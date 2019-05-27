package com.lenta.bp10.mobileObjectsService.models

import com.google.gson.annotations.SerializedName

// Модель справочника закрепления причин списания брака за секциями (WOB)
class MB_S_31(
        /// Тип задания на списание
        @SerializedName("TASK_TYPE")
        val taskType: String,

        /// Номер отдела
        @SerializedName("SECTION_ID")
        val sectionId: String,

        /// Причина движения
        @SerializedName("REASON")
        val reason: String,

        /// Текст к причине движения товара
        @SerializedName("GRTXT")
        val grtxt: String
)