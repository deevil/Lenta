package com.lenta.bp16.model.ingredients.ui

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Данные ШК по товарам
 */
@Parcelize
data class OrderByBarcodeUI(
        /** SAP-код товара */
        val matnr: String,

        /** Глобальный номер товара (GTIN) */
        val ean: String,

        /** Единица измерения */
        val ean_nom: String,

        /** Числитель для пересчета в базисные единицы измерения */
        val ean_umrez: String,

        /** Знаменатель при пересчете в базисные единицы измерения */
        val ean_umren: String
) : Parcelable