package com.lenta.bp16.model.ingredients.results

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.model.ingredients.VetBatchDataInfo
import com.lenta.bp16.model.ingredients.ui.MercuryDataInfoResultUI
import com.lenta.bp16.platform.converter.IConvertable

data class MercuryDataInfoResult(
        /** Данные по меркурианской партии */
        @SerializedName("ET_VET_BATCH")
        val batchList: List<VetBatchDataInfo>?
) : IConvertable<MercuryDataInfoResultUI> {
    override fun convert(): MercuryDataInfoResultUI {
        return MercuryDataInfoResultUI(
                batchList = batchList.orEmpty().mapNotNull { it.convert() }
        )
    }
}