package com.lenta.bp16.model.ingredients.results

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.request.pojo.DataLabel
import com.lenta.shared.utilities.extentions.IResultWithRetCodes
import com.lenta.shared.utilities.extentions.IRetCode

data class MaterialDataCompleteResult (

        /** Код тары */
        @SerializedName("EV_CODE_CONT_ING")
        val packCode: String?,

        /** Структура данных для этикетки */
        @SerializedName("ES_DATA_LABEL")
        val dataLabel: DataLabel?,

        /**Таблица возврата */
        @SerializedName("ET_RETCODE")
        override val retCodes: List<IRetCode>?
): IResultWithRetCodes