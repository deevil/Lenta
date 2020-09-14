package com.lenta.bp16.model.ingredients

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.lenta.bp16.model.ingredients.ui.MercuryPartDataInfoUI
import com.lenta.bp16.platform.converter.IConvertable
import kotlinx.android.parcel.Parcelize

/**
 * Данные по меркурианским партиям
 * */
@Parcelize
data class MercuryPartDataInfo(

        /**SAP-код товара*/
        @SerializedName("MANTR")
        val matnr: String?,

        /**Номер ЗСЖ*/
        @SerializedName("ENTRYID")
        val entryId: String?,

        /**GUID Производителя*/
        @SerializedName("ZPROD")
        val zProd: String?,

        /**Наименование производителя*/
        @SerializedName("PROD_NAME")
        val prodName: String?,

        /**Дата производства*/
        @SerializedName("PROD_DATE")
        val prodDate: String?

) : Parcelable, IConvertable<MercuryPartDataInfoUI?> {
    override fun convert(): MercuryPartDataInfoUI? {
        return MercuryPartDataInfoUI(
                matnr = matnr.orEmpty(),
                entryId = entryId.orEmpty(),
                zProd = zProd.orEmpty(),
                prodName = prodName.orEmpty(),
                prodDate = prodDate.orEmpty()
        )
    }
}