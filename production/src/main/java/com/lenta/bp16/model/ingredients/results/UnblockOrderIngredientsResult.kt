package com.lenta.bp16.model.ingredients.results

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.platform.extention.IResultWithRetCodes
import com.lenta.bp16.request.pojo.RetCode

data class UnblockOrderIngredientsResult(
        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        override val retCodes: List<RetCode>?
) : IResultWithRetCodes