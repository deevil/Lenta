package com.lenta.bp10.mobileObjectsService.models

import com.google.gson.annotations.SerializedName

class MB_S_29(
        /// Аббревиатура типа задания на списание
        @SerializedName("TASK_TYPE")
        val taskType: String,

        /// Вид движения (управление запасами)
        @SerializedName("BWART")
        val bwart: String,

        /// Место возникновения затрат
        @SerializedName("KOSTL")
        val kostl: String,

        /// Склад
        @SerializedName("LGORTTO")
        val lgortto: String,

        /// Требуется отправка в ГИС
        @SerializedName("SEND_GIS")
        val sendGis: String,

        /// Признак, что для данного типа задания выбирать причину списания не нужно
        @SerializedName("NO_GRUND")
        val noGrund: String,

        /// Полное название типа задания на списание
        @SerializedName("LONG_NAME")
        val longName: String,

        /// Лимит количества товаров в задании
        @SerializedName("LIMIT")
        val limit: Double,

        /// Используется в производстве
        @SerializedName("CHK_OWNPR")
        val chkOwnpr: String
)