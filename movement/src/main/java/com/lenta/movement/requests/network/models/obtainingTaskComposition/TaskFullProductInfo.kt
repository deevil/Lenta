package com.lenta.movement.requests.network.models.obtainingTaskComposition

import com.google.gson.annotations.SerializedName

/**"ET_MATERIALS"*/
data class TaskFullProductInfo(
        /** SAP-код товара */
        @SerializedName("MATNR")
        val materialNumber: String,

        /** Длинное наименование */
        @SerializedName("NAME")
        val name: String,

        /** Вид товара */
        @SerializedName("MATYPE")
        val productType: String,

        /** Базисная единица измерения */
        @SerializedName("BUOM")
        val units: String,

        /** Тип матрицы SKU */
        @SerializedName("MATR_TYPE")
        val matrixTypeSKU: String,

        /** Номер отдела (секция) */
        @SerializedName("ABTNR")
        val departmentNumber: String,

        /** Признак – товар акцизный */
        @SerializedName("IS_EXC")
        val isExc: String,

        /** Признак - товар алкогольный */
        @SerializedName("IS_ALCO")
        val isAlco: String,

        /** Группа товаров */
        @SerializedName("MATKL")
        val goodGroup: String,

        /** Группа закупок */
        @SerializedName("EKGRP")
        val purchaseGroup: String,

        /** Единица измерения заказа */
        @SerializedName("BSTME")
        val orderUnits: String,

        /** Количество вложений */
        @SerializedName("QNTINCL")
        val quantityInvestments: String,

        /** Признак: отечественный */
        @SerializedName("IS_RUS")
        val isRus: String,

        /** Признак: ветеринарный контроль*/
        @SerializedName("IS_VET")
        val isVet: String,

        /** Индикатор: Товар «Еда» */
        @SerializedName("IS_FOOD")
        val isFood: String,

        /** Объем в куб.метрах  (точность 6 знаков) */
        @SerializedName("VOLUM")
        val volume: String,

        /** Единица объема */
        @SerializedName("VOLEH")
        val volumeUnit: String,

        /** Тип маркировки */
        @SerializedName("ZMARKTYPE")
        val markType: String)