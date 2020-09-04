package com.lenta.bp16.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BatchNewDataInfo(

        /** Код производителя */
        @SerializedName("PRODUCER")
        val prodCode: String,

        /** Дата производства */
        @SerializedName("PROD_DATE")
        val prodDate: String,

        /** Время производства */
        @SerializedName("PROD_TIME")
        val prodTime: String,

        /**Склад*/
        @SerializedName("LGORT")
        val lgort: String

) : Parcelable