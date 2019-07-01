package com.lenta.shared.models.core

import com.lenta.shared.R

enum class ProductType {
    General,
    NonExciseAlcohol,
    ExciseAlcohol
}


fun getProductType(isAlco: Boolean, isExcise: Boolean): ProductType {
    if (isAlco) {
        return if (isExcise) ProductType.ExciseAlcohol else ProductType.NonExciseAlcohol
    }
    return ProductType.General
}

fun ProductType?.getDescriptionResId(): Int {
    return when (this) {
        ProductType.General ->  R.string.general_product
        ProductType.ExciseAlcohol -> R.string.excise_alco
        ProductType.NonExciseAlcohol -> R.string.non_excise_alco
        else -> 0
    }
}