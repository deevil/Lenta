package com.lenta.bp16.model.ingredients

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.model.ingredients.ui.GoodByOrderUI
import com.lenta.bp16.platform.converter.IConvertable

//Список товаров по определенным заказам
data class GoodByOrder(
        /** Номер технологического заказа */
        @SerializedName("AUFNR")
        val aufnr: String?,
        /** SAP-код ингредиента */
        @SerializedName("MATNR")
        val matnr: String?
) : IConvertable<GoodByOrderUI?> {
        override fun convert(): GoodByOrderUI? {
                return GoodByOrderUI(
                        aufnr = aufnr.orEmpty(),
                        matnr = matnr.orEmpty()
                )
        }
}