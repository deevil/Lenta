package com.lenta.bp10.mobileObjectsService.models

import com.google.gson.annotations.SerializedName

// Модель справочника настроек заданий на списание WOB (справочник видов товара)
class MB_S_34(/// Тип задания на списание
        @SerializedName("TASK_TYPE")
        val taskType: String,

        /// Вид товара
        @SerializedName("MTART")
        val mtart: String
)