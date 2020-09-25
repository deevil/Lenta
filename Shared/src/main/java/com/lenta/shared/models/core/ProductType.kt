package com.lenta.shared.models.core

import com.lenta.shared.R

enum class ProductType(val code: String) {
    General("N"),
    NonExciseAlcohol("A"),
    ExciseAlcohol("A"),
    Marked("M"),
    ZBatch("Z"),
    Vet("V"),
    Unknown("UNKNOWN")
}

fun getProductType(isAlco: Boolean, isExcise: Boolean, isMarkedGood: Boolean = false, isZBatch: Boolean = false, isVet: Boolean = false): ProductType {
    return when{
        isMarkedGood -> ProductType.Marked
        isExcise -> ProductType.ExciseAlcohol
        isAlco -> ProductType.NonExciseAlcohol
        isZBatch -> ProductType.ZBatch
        isVet -> ProductType.Vet
        else -> ProductType.General
    }
}

fun ProductType?.getDescriptionResId(): Int {
    return when (this) {
        ProductType.General ->  R.string.general_product
        ProductType.ExciseAlcohol -> R.string.excise_alco
        ProductType.NonExciseAlcohol -> R.string.non_excise_alco
        ProductType.Marked -> R.string.marked_good
        ProductType.ZBatch -> R.string.zbatch_goods
        ProductType.Vet -> R.string.vet_good
        else -> 0
    }
}