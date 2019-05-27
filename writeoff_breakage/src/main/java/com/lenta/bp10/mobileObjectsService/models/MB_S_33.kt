package com.lenta.bp10.mobileObjectsService.models

import com.google.gson.annotations.SerializedName

// Модель справочника настроек заданий на списание WOB (справочник складов)
class MB_S_33(
        /// Тип задания на списание
        @SerializedName("TASK_TYPE")
        val taskType: String,

        /// ТК
        @SerializedName("WERKS")
        val werks: String,

        /// Склад
        @SerializedName("LGORT")
        val lgort: String
)