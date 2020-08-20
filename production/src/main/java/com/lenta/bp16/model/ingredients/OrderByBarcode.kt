package com.lenta.bp16.model.ingredients

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.lenta.bp16.model.ingredients.ui.OrderByBarcodeUI
import com.lenta.bp16.platform.converter.IConvertable
import kotlinx.android.parcel.Parcelize

/**
 * Данные ШК по товарам
 */
@Parcelize
data class OrderByBarcode(
        /** SAP-код товара */
        @SerializedName("MATNR")
        val matnr: String?,

        /** Глобальный номер товара (GTIN) */
        @SerializedName("EAN")
        val ean: String?,

        /** Единица измерения */
        @SerializedName("EAN_UOM")
        val ean_nom: String?,

        /** Числитель для пересчета в базисные единицы измерения */
        @SerializedName("EAN_UMREZ")
        val ean_umrez: String?,

        /** Знаменатель при пересчете в базисные единицы измерения */
        @SerializedName("EAN_UMREN")
        val ean_umren: String?
) : Parcelable, IConvertable<OrderByBarcodeUI?>
{
        companion object{
                const val KAR = "KAR"
                const val ST = "ST"
                const val KG = "KG"
        }

        override fun convert(): OrderByBarcodeUI? {
                return matnr?.run {
                        OrderByBarcodeUI(
                                matnr = matnr,
                                ean = ean.orEmpty(),
                                ean_nom = ean_nom.orEmpty(),
                                ean_umren = ean_umren.orEmpty(),
                                ean_umrez = ean_umrez.orEmpty()
                        )
                }
        }
}