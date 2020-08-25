package com.lenta.bp16.model.ingredients.results

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.model.ingredients.GoodByOrder
import com.lenta.bp16.model.ingredients.IngredientInfo
import com.lenta.bp16.model.ingredients.OrderByBarcode
import com.lenta.bp16.model.ingredients.ui.IngredientsListResultUI
import com.lenta.bp16.platform.converter.IConvertable
import com.lenta.bp16.request.pojo.RetCode
import com.lenta.shared.utilities.extentions.IResultWithRetCodes

data class IngredientsListResult(
        /** список всех ингредиентов по заказам и по материалам */
        @SerializedName("ET_OBJ_LIST")
        val ingredientsList: List<IngredientInfo>?,

        @SerializedName("ET_AUFNR_DATA")
        val goodsListByOrder: List<GoodByOrder>?,

        @SerializedName("ET_EAN")
        val goodsEanList: List<OrderByBarcode>?,

        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        override val retCodes: List<RetCode>?
) : IResultWithRetCodes, IConvertable<IngredientsListResultUI?> {

    override fun convert(): IngredientsListResultUI? {
        return IngredientsListResultUI(
                ingredientsList = ingredientsList.orEmpty(),
                goodsListByOrder = goodsListByOrder.orEmpty(),
                goodsEanList = goodsEanList.orEmpty(),
                retCodes = retCodes.orEmpty()
        )
    }
}