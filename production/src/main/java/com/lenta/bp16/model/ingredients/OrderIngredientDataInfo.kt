package com.lenta.bp16.model.ingredients

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.lenta.bp16.model.ingredients.ui.OrderIngredientDataInfoUI
import com.lenta.bp16.platform.converter.IConvertable
import com.lenta.shared.utilities.orIfNull
import kotlinx.android.parcel.Parcelize

/**
 * Данные по заказу
 */
@Parcelize
data class OrderIngredientDataInfo(
        /**
         * SAP – код товара (ингредиент)
         */
        @SerializedName("MATNR")
        val matnr: String?,

        /**
         * Наименование товара (ингредиент)
         */
        @SerializedName("NAME_MATNR")
        val name: String?,

        /**
         * Единица измерения товара
         */
        @SerializedName("BUOM")
        val buom: String?,

        /**
         * Расчетное количество ингредиента в технологическом заказе
         */
        @SerializedName("PLAN_QNT")
        val plan_qnt: Double?,

        /**
         * Скомплектованное количество ингредиента в рамках данного заказа
         */
        @SerializedName("DONE_QNT")
        val done_qnt: Double?,

        /**
         * Признак товара релевантен Z-партионному учету
         * */
        @SerializedName("IS_ZPART")
        val isZpart: String?,

        /**
         * Признак Меркурианский товар
         * */
        @SerializedName("IS_VET")
        val isVet: String?,

        /**
         * Признак "Товар списывается в производство по факту"
         * */
        @SerializedName("IS_FACT")
        val isFact: String?,

        /**
         * Общий срок годности товара
         * */
        @SerializedName("MHDHB")
        val shelfLife: String?

) : Parcelable, IConvertable<OrderIngredientDataInfoUI?> {

    override fun convert(): OrderIngredientDataInfoUI? {
        return OrderIngredientDataInfoUI(
                matnr = matnr.orEmpty(),
                name = name.orEmpty(),
                buom = buom.orEmpty(),
                plan_qnt = plan_qnt.orIfNull { 0.0 },
                done_qnt = done_qnt.orIfNull { 0.0 },
                isZpart = isZpart.orEmpty(),
                isVet = isVet.orEmpty(),
                isFact = isFact.orEmpty(),
                shelfLife = shelfLife.orEmpty()
        )
    }
}