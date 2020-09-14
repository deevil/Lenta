package com.lenta.bp16.model.ingredients.ui

import android.os.Parcelable
import com.lenta.shared.models.core.toUom
import com.lenta.shared.utilities.extentions.dropZeros
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OrderIngredientDataInfoUI(

        /** SAP – код товара (ингредиент) */
        val matnr: String,

        /** Наименование товара (ингредиент) */
        val name: String,

        /** Единица измерения товара */
        val buom: String,

        /** Расчетное количество ингредиента в технологическом заказе */
        val plan_qnt: Double,

        /** Скомплектованное количество ингредиента в рамках данного заказа */
        val done_qnt: Double,

        /** Признак товара релевантен Z-партионному учету */
        val isZpart: String,

        /** Признак Меркурианский товар */
        val isVet: String,

        /** Признак "Товар списывается в производство по факту" */
        val isFact: String,

        /** Общий срок годности товара */
        val shelfLife: String

) : Parcelable {
    fun getSuffix(): String {
        return buom.toUom().name
    }

    fun getPlanCount(): String {
        return buildString {
            append(plan_qnt.dropZeros())
            append(" ")
            append(getSuffix())
        }
    }

    fun getDoneCount(): String {
        return buildString {
            append(done_qnt.dropZeros())
            append(" ")
            append(getSuffix())
        }
    }

    fun getFormattedMaterial(): String {
        return matnr.takeLast(6)
    }
}