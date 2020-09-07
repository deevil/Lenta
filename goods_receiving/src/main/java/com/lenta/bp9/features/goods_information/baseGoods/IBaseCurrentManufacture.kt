package com.lenta.bp9.features.goods_information.baseGoods

interface IBaseCurrentManufacture : IBaseVariables {
    val currentManufacture: String
        get() {
            val position = spinManufacturersSelectedPosition.value ?: -1
            return position
                    .takeIf { it >= 0 }
                    ?.run {
                        spinManufacturers.value
                                ?.takeIf { it.isNotEmpty() }
                                ?.run { this[position] }
                                .orEmpty()
                    }
                    .orEmpty()
        }
}