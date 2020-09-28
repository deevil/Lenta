package com.lenta.bp14.requests.pojo

import com.google.gson.annotations.SerializedName

data class ProductInfo(
        /** SAP-код товара */
        @SerializedName("MATNR")
        val matNr: String,
        /** Длинное наименование */
        @SerializedName("NAME")
        val name: String,
        /** ЕИ заказа на поставку */
        @SerializedName("BSTME")
        val bSTME: String,
        /** Базисная единица измерения */
        @SerializedName("BUOM")
        val bUom: String,
        /** Общий срок годности в днях */
        @SerializedName("MHDHB_DAYS")
        val expirationDate: Int,
        /** Остаточный срок годности в днях */
        @SerializedName("MHDRZ_DAYS")
        val remainingExpirationDate: Int,
        /** Группа товаров */
        @SerializedName("MATKL")
        val matKL: String,
        /** Группа закупок */
        @SerializedName("EKGRP")
        val eKGRP: String,
        /** Тип матрицы SKU */
        @SerializedName("MATR_TYPE")
        val matrixType: String,
        /** Номер отдела (секция) */
        @SerializedName("ABTNR")
        val sectionNumber: String,
        /** Признак - акцизный алкоголь */
        @SerializedName("IS_EXC")
        val isExcise: String,
        /** Признак - товар алкоголь */
        @SerializedName("IS_ALCO")
        val isAlco: String,
        /** Признак – товар маркированный */
        @SerializedName("IS_MARK")
        val isMark: String,
        /** Признак – товар ветеринарный */
        @SerializedName("IS_VET")
        val isVet: String,
        /**Признак - "Вино России"*/
        @SerializedName("IS_VRUS")
        val isRusWine: String,
        /** Признак – товар здоровое питание */
        @SerializedName("IS_HF")
        val isHealthyFood: String,
        /** Признак - является ли товар z-партией */
        @SerializedName("IS_ZPARTS")
        val isZPart: String?,
        /** Признак – товар новинка */
        @SerializedName("IS_NEW")
        val isNew: String,
        /** Глобальный номер товара (GTIN) */
        @SerializedName("EAN")
        val ean: String,
        /** Единица измерения */
        @SerializedName("EAN_UOM")
        val eanUOM: String,
        /** Числитель для пересчета в базисные единицы измерения */
        @SerializedName("EAN_UMREZ")
        val eanUmRez: String,
        /** Знаменатель при пересчете в базисные единицы измерения */
        @SerializedName("EAN_UMREN")
        val eanUmRen: String
)