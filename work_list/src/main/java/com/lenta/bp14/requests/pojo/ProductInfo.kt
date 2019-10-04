package com.lenta.bp14.requests.pojo

import com.google.gson.annotations.SerializedName

data class ProductInfo(
        //Номер отдела (секция)
        @SerializedName("ABTNR")
        val sectionNumber: String,
        //ЕИ заказа на поставку
        @SerializedName("BSTME")
        val bSTME: String,
        //Базисная единица измерения
        @SerializedName("BUOM")
        val bUom: String,
        //Глобальный номер товара (GTIN)
        @SerializedName("EAN")
        val ean: String,
        //Знаменатель при пересчете в базисные единицы измерения
        @SerializedName("EAN_UMREN")
        val eanUmRen: String,
        //Числитель для пересчета в базисные единицы измерения
        @SerializedName("EAN_UMREZ")
        val eanUmRez: String,
        @SerializedName("EAN_UOM")
        //Единица измерения
        val eanUOM: String,
        //Группа закупок
        @SerializedName("EKGRP")
        val eKGRP: String,
        //Признак - Товар алкоголь
        @SerializedName("IS_ALCO")
        val isAlco: String,
        //Признак - Акцизный алкоголь
        @SerializedName("IS_EXC")
        val isExcise: String,
        //Признак – товар здоровое питание
        @SerializedName("IS_HF")
        val isHealthyFood: String,
        //Признак – Товар маркированный
        @SerializedName("IS_MARK")
        val iSMarked: String,
        //Признак – товар новинка
        @SerializedName("IS_NEW")
        val isNew: String,
        //Признак – товар ветеринарный
        @SerializedName("IS_VET")
        val isVet: String,
        //SAP-код товара
        @SerializedName("MATNR")
        val matNr: String,
        //Группа товаров
        @SerializedName("MATKL")
        val matKL: String,
        //Тип матрицы SKU
        @SerializedName("MATR_TYPE")
        val matrixType: String,
        //Общий срок годности в днях
        @SerializedName("MHDHB_DAYS")
        val expirationDate: Int,
        //Остаточный срок годности в днях
        @SerializedName("MHDRZ_DAYS")
        val remainingExpirationDate: Int,
        //Длинное наименование
        @SerializedName("NAME")
        val name: String
)