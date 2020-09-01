package com.lenta.bp16.model.ingredients.ui

import com.lenta.bp16.model.IngredientStatusBlock
import com.lenta.bp16.model.IngredientStatusWork

data class ItemIngredientUi(
        val code: String,
        val position: String,
        val text1: String,
        val text2: String,
        val ingredientStatusBlock: IngredientStatusBlock,
        val ingredientStatusWork: IngredientStatusWork
)