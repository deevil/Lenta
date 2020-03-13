package com.lenta.bp12.model.pojo

import com.google.gson.annotations.SerializedName

data class MaterialInfo(
        /** Sap-код товара */
        @SerializedName("MATERIAL")
        var material: String,
        /** Наименование */
        @SerializedName("NAME")
        var name: String,
        /** Вид товара */
        @SerializedName("MATYPE")
        var goodType: String,
        /** Базовая единица измерения */
        @SerializedName("BUOM")
        var unitCode: String,
        /** Тип матрицы SCU */
        @SerializedName("MATR_TYPE")
        var matrix: String,
        /** Номер секции */
        @SerializedName("ABTNR")
        var section: String,
        /** Акцизный товар */
        @SerializedName("IS_EXC")
        var isExcise: String,
        /** Алкоголь */
        @SerializedName("IS_ALCO")
        var isAlcohol: String,
        /** Группа товара */
        @SerializedName("MATKL")
        var goodGroup: String,
        /** Группа закупки */
        @SerializedName("EKGRP")
        var purchaseGroup: String,
        /** ЕИ заказа на поставку */
        @SerializedName("BSTME")
        var unitCodeSupply: String,
        /** Количество вложенного */
        @SerializedName("QNTINCL")
        var innerQuantity: String,
        /** Общий флаг */
        @SerializedName("IS_RUS")
        var isRus: String,
        /** Общий флаг */
        @SerializedName("IS_VET")
        var isVet: String,
        /** Общий флаг */
        @SerializedName("IS_FOOD") // Для BKS не используется
        var isFood: String,
        /** Обьъем в куб. метрах (точность 6 знаков) */
        @SerializedName("VOLUM") // Для BKS не используется
        var volume: String,
        /** Единица объема */
        @SerializedName("VOLEH") // Для BKS не используется
        var volumeUnit: String
)