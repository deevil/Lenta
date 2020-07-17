package com.lenta.bp16.model.ingredients

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.request.pojo.IngredientInfo
import com.lenta.bp16.request.pojo.RetCode

data class IngredientsListResult(
        // список
        @SerializedName("ET_OBJ_LIST")
        val ingredientsList: List<IngredientInfo>,

        @SerializedName("ET_AUFNR_DATA")
        val goodsListByOrder: List<Any>,

        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        val retCodes: List<RetCode>
)