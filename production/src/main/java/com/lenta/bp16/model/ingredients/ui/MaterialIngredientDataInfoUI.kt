package com.lenta.bp16.model.ingredients.ui

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/** Данные по материалу */
@Parcelize
data class MaterialIngredientDataInfoUI(
        /** Код передела (ключ образца) */
        val ktsch: String,

        /** Склад */
        val lgort: String,

        /** Наименование операции (краткий текст операции) */
        val ltxa1: String,

        /** Наименование товара (ингредиент) */
        val name: String,

        /** Расчетное количество ингредиента в технологическом заказе */
        val plan_qnt: String,

        /** Скомплектованное количество ингредиента в рамках данного заказа */
        val done_qnt: String,

        /** Признак товар релевантен Z-партионному учету */
        val isZpart: String,

        /** Признак Меркурианский товар */
        val isVet: String,

        /** Признак "Товар списывается в производство по факту" */
        val isFact: String,

        /** Общий срок годности */
        val shelfLife: String
): Parcelable