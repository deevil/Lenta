package com.lenta.bp16.model.ingredients

import com.lenta.bp16.model.IngredientStatus

data class ItemIngredientUi(
        val position: String,
        val text1: String,
        val text2: String,
        val ingredientStatus: IngredientStatus
)