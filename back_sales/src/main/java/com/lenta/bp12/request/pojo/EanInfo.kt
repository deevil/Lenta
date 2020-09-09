package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName

data class EanInfo(
        /** ШК товара */
        @SerializedName("EAN")
        var ean: String?,
        /** Sap-код товара */
        @SerializedName("MATERIAL")
        var material: String?,
        /** Код единицы измерения */
        @SerializedName("UOM")
        var unitCode: String?,
        /** Числитель для пересчета в базисные ИЕ */
        @SerializedName("UMREZ")
        var umrez: String?,
        /** Знаменатель для пересчета в базисные ИЕ */
        @SerializedName("UMREN")
        var umren: String?,
        /** Индикатор "Исключен из маркировки" */
        @SerializedName("ZE_NONMARK")
        val nonMark: String?
)