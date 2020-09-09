package com.lenta.bp16.model.ingredients

import com.google.gson.annotations.SerializedName

data class VetBatchDataInfo(

        /** Номер ЗСЖ */
        @SerializedName("ENTRYID")
        val entryId: String?,

        /** Наименование производителя */
        @SerializedName("PROD_NAME")
        val prodName: String?,

        /** Код производителя */
        @SerializedName("ZPROD")
        val prodCode: String?,

        /** Дата производства */
        @SerializedName("PROD_DATE")
        val prodDate: String?
)