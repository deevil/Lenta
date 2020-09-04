package com.lenta.bp16.model.ingredients.results

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.request.pojo.DataLabel
import com.lenta.bp16.request.pojo.RetCode
import com.lenta.shared.utilities.extentions.IResultWithRetCodes

data class IngredientDataCompleteResult(

        /** Код тары */
        @SerializedName("EV_CODE_CONT")
        val packCode: String?,

        /** Признак «Была произведена фиксация на следующем переделе»  */
        @SerializedName("EV_AUTOFIX")
        val isAutofix: Boolean?,

        /** Структура данных для этикетки */
        @SerializedName("ES_DATA_LABEL")
        val dataLabel: DataLabel?,

        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        override val retCodes: List<RetCode>?
) : IResultWithRetCodes