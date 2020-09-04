package com.lenta.bp16.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Данные по Z-партиям
 * */
@Parcelize
data class ZPartDateInfo(

        /** SAP-код */
        @SerializedName("MATNR")
        val matnr: String?,

        /** Номер партии */
        @SerializedName("BATCHID")
        val batchId: String?,

        /** Код производителя */
        @SerializedName("PRODUCER")
        val prodCode: String?,

        /** Наименование производителя */
        @SerializedName("PROD_NAME")
        val prodName: String?,

        /** Дата производства */
        @SerializedName("PROD_DATE")
        val prodDate: String?
) : Parcelable