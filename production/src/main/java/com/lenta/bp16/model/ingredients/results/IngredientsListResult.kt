package com.lenta.bp16.model.ingredients.results

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.model.ingredients.GoodByOrder
import com.lenta.bp16.model.ingredients.IngredientInfo
import com.lenta.bp16.platform.extention.IResultWithRetCodes
import com.lenta.bp16.request.pojo.RetCode

data class IngredientsListResult(
        // список всех ингредиентов по заказам и по материалам
        @SerializedName("ET_OBJ_LIST")
        val ingredientsList: List<IngredientInfo>,

        @SerializedName("ET_AUFNR_DATA")
        val goodsListByOrder: List<GoodByOrder>,

        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        override val retCodes: List<RetCode>
) : IResultWithRetCodes