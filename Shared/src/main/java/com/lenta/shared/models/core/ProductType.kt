package com.lenta.shared.models.core

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