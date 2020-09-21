package com.lenta.bp9.features.goods_information.base

interface IBaseSpinManufacture : IBaseVariables {
    val currentManufactureName: String
        get() {
            val position = spinManufacturersSelectedPosition.value ?: -1
            return position
                    .takeIf { it >= 0 }
                    ?.let {
                        spinManufacturers.value
                                ?.takeIf { it.isNotEmpty() }
                                ?.let { it[position] }
                                .orEmpty()
                    }
                    .orEmpty()
        }
}