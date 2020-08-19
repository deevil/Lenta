package com.lenta.bp16.model.ingredients.results

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.request.pojo.RetCode
import com.lenta.shared.utilities.extentions.IResultWithRetCodes

data class UnblockOrderIngredientsResult(
        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        override val retCodes: List<RetCode>?
) : IResultWithRetCodes