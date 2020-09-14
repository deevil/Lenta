package com.lenta.bp16.model.ingredients

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.lenta.bp16.model.ingredients.ui.MaterialIngredientDataInfoUI
import com.lenta.bp16.platform.converter.IConvertable
import kotlinx.android.parcel.Parcelize

/**
 * Данные по материалу
 */
@Parcelize
data class MaterialIngredientDataInfo(
        /**
         * Код передела (ключ образца)
         */
        @SerializedName("KTSCH")
        val ktsch: String?,

        /**
         * Склад
         */
        @SerializedName("LGORT")
        val lgort: String?,

        /**
         * Наименование операции (краткий текст операции)
         */
        @SerializedName("LTXA1")
        val ltxa1: String?,

        /**
         * Наименование товара (ингредиент)
         */
        @SerializedName("NAME_MATNR")
        val name: String?,

        /**
         * Расчетное количество ингредиента в технологическом заказе
         */
        @SerializedName("PLAN_QNT")
        val plan_qnt: String?,

        /**
         * Скомплектованное количество ингредиента в рамках данного заказа
         */
        @SerializedName("DONE_QNT")
        val done_qnt: String?,

        /**
         * Признак товар релевантен Z-партионному учету
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
         * Общий срок годности
         * */
        @SerializedName("MHDHB")
        val shelfLife: String?
) : Parcelable, IConvertable<MaterialIngredientDataInfoUI?> {
    override fun convert(): MaterialIngredientDataInfoUI? {
        return MaterialIngredientDataInfoUI(
                ktsch = ktsch.orEmpty(),
                lgort = lgort.orEmpty(),
                ltxa1 = ltxa1.orEmpty(),
                name = name.orEmpty(),
                plan_qnt = plan_qnt.orEmpty(),
                done_qnt = done_qnt.orEmpty(),
                isZpart = isZpart.orEmpty(),
                isVet = isVet.orEmpty(),
                isFact = isFact.orEmpty(),
                shelfLife = shelfLife.orEmpty()
        )
    }
}