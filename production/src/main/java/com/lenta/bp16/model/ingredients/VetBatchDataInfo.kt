package com.lenta.bp16.model.ingredients

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.model.ingredients.ui.VetBatchDataInfoUI
import com.lenta.bp16.platform.converter.IConvertable

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
) : IConvertable<VetBatchDataInfoUI?> {
    override fun convert(): VetBatchDataInfoUI? {
        return VetBatchDataInfoUI(
                entryId = entryId.orEmpty(),
                prodName = prodName.orEmpty(),
                prodCode = prodCode.orEmpty(),
                prodDate = prodDate.orEmpty()
        )
    }
}