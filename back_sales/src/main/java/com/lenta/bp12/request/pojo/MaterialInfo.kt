package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName

data class MaterialInfo(
        /** Sap-код товара */
        @SerializedName("MATERIAL")
        val material: String?,
        /** Наименование */
        @SerializedName("NAME")
        val name: String?,
        /** Вид товара */
        @SerializedName("MATYPE")
        val goodType: String?,
        /** Базовая единица измерения */
        @SerializedName("BUOM")
        val innerUnitsCode: String?,
        /** Тип матрицы SCU */
        @SerializedName("MATR_TYPE")
        val matrix: String?,
        /** Номер секции */
        @SerializedName("ABTNR")
        val section: String?,
        /** Акцизный товар */
        @SerializedName("IS_EXC")
        val isExcise: String?,
        /** Алкоголь */
        @SerializedName("IS_ALCO")
        val isAlcohol: String?,
        /** Группа товара */
        @SerializedName("MATKL")
        val goodGroup: String?,
        /** Группа закупки */
        @SerializedName("EKGRP")
        val purchaseGroup: String?,
        /** ЕИ заказа на поставку */
        @SerializedName("BSTME")
        val commonUnitsCode: String?,
        /** Количество вложенного */
        @SerializedName("QNTINCL")
        val innerQuantity: String?,
        /** Общий флаг */
        @SerializedName("IS_RUS")
        val isRus: String?,
        /** Общий флаг */
        @SerializedName("IS_VET")
        val isVet: String?,
        /** Общий флаг */
        @SerializedName("IS_FOOD") // Для BKS не используется
        val isFood: String?,
        /** Обьъем в куб. метрах (точность 6 знаков) */
        @SerializedName("VOLUM")
        val volume: String?,
        /** Единица объема */
        @SerializedName("VOLEH")
        val volumeUnit: String?
        /** Тип маркировки */
        @SerializedName("ZMARKTYPE")
        val markType: String?

)