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
{
        companion object{
                const val FAKE_MATNR = ""
                const val FAKE_EAN = "0000000000000"
                const val EAN_NOM = ""
                const val EAN_UMREZ = ""
                const val EAN_UMREN = ""
        }
}