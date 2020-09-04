package com.lenta.bp16.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Справочник производителей
 * */
@Parcelize
data class ProducerDataInfo(

        /** Наименование товара */
        @SerializedName("MATNR")
        val mantr: String?,

        /** Код производителя */
        @SerializedName("PRODUCER")
        val prodCode: String?,

        /** Номер EAN */
        @SerializedName("EAN")
        val ean: String?,

        /** Наименование пользователя */
        @SerializedName("PROD_NAME")
        val prodName: String?

) : Parcelable