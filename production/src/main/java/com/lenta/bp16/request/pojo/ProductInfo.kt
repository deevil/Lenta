package com.lenta.bp16.request.pojo

import com.google.gson.annotations.SerializedName

data class ProductInfo(
        /**Номер товара*/
        @SerializedName("MATNR")
        val productNumber: String?,
        /**Длинное наименование*/
        @SerializedName("NAME")
        val productName: String?,
        /**Вид товара*/
        @SerializedName("MATYPE")
        val productType: String?,
        /**Старый номер товара*/
        @SerializedName("MATCODE")
        val productMatcode: String?,
        /**Базисная единица измерения*/
        @SerializedName("BUOM")
        val buom: String?,
        /**ЕИ заказа на поставку*/
        @SerializedName("BSTME")
        val bstme: String?,
        /**Тип матрицы SKU*/
        @SerializedName("MATR_TYPE")
        val matrType: String?,
        /**Номер отдела*/
        @SerializedName("ABTNR")
        val abtnr: String?,
        /**Общий флаг*/
        @SerializedName("IS_RETURN")
        val isReturn: String?,
        /**Признак – товар акцизный*/
        @SerializedName("IS_EXC")
        val isExc: String?,
        /**Общий флаг*/
        @SerializedName("IS_ALCO")
        val isAlso: String?,
        /**Общий флаг*/
        @SerializedName("IS_SET")
        val isSet: String?,
        /**Группа товаров*/
        @SerializedName("MATKL")
        val matkl: String?,
        /**Группа закупок*/
        @SerializedName("EKGRP")
        val ekgrp: String?,
        /**Общий срок годности в днях*/
        @SerializedName("MHDHB_DAYS")
        val mhdhbDays: Int?,
        /**Остаточный срок годности в днях*/
        @SerializedName("MHDRZ_DAYS")
        val mhdrzDays: Int?,
        /**Глобальный номер товара (GTIN)*/
        @SerializedName("EAN")
        val ean: String?,
        /**Единица измерения*/
        @SerializedName("EAN_UOM")
        val uom: String?,
        /**Числитель для пересчета в базисные единицы измерения*/
        @SerializedName("EAN_UMREZ")
        val umrez: String?,
        /**Знаменатель при пересчете в базисные единицы измерения*/
        @SerializedName("EAN_UMREN")
        val umren: String?,
        /**Общий флаг*/
        @SerializedName("IS_MARK")
        val isMark: String?,
        /**Общий флаг*/
        @SerializedName("IS_VET")
        val isVet: String?,
        /**Общий флаг*/
        @SerializedName("IS_HF")
        val isHf: String?,
        /**Общий флаг*/
        @SerializedName("IS_NEW")
        val isNew: String?,
        /**Маркировка*/
        @SerializedName("ZMARKTYPE")
        val markType: String?,
        /**Общий флаг*/
        @SerializedName("IS_MRK_MIX")
        val isMrkMix: String?,
        /**Общий флаг*/
        @SerializedName("IS_ZPART")
        val isPart: String?

)