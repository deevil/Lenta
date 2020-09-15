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

        /** Единица измерения товара (план)*/
        val buomPln: String,

        /** Единица измерения товара (скоплектовано)*/
        val buomDone: String,

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
    fun getPlnSuffix(): String {
        return buomPln.toUom().name
    }

    fun getDoneSuffix(): String {
        return buomDone.toUom().name
    }

    fun getPlanCount(): String {
        return buildString {
            append(plan_qnt.dropZeros())
            append(" ")
            append(getPlnSuffix())
        }
    }

    fun getDoneCount(): String {
        return buildString {
            append(done_qnt.dropZeros())
            append(" ")
            append(getDoneSuffix())
        }
    }

    fun getFormattedMaterial(): String {
        return matnr.takeLast(6)
    }
}