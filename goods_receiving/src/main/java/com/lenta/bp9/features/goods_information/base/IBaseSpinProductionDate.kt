package com.lenta.bp9.features.goods_information.base

interface IBaseSpinProductionDate : IBaseVariables {
    val currentProductionDate: String
        get() {
            val position = spinProductionDateSelectedPosition.value ?: -1
            return position
                    .takeIf { it >= 0 }
                    ?.let {
                        spinProductionDate.value
                                ?.takeIf { it.isNotEmpty() }
                                ?.let { it[position] }
                                .orEmpty()
                    }
                    .orEmpty()
        }
}