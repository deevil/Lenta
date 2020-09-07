package com.lenta.bp9.features.goods_information.baseGoods

interface IBaseCurrentProductionDate : IBaseVariables {
    val currentProductionDate: String
        get() {
            val position = spinProductionDateSelectedPosition.value ?: -1
            return position
                    .takeIf { it >= 0 }
                    ?.run {
                        spinProductionDate.value
                                ?.takeIf { it.isNotEmpty() }
                                ?.run { this[position] }
                                .orEmpty()
                    }
                    .orEmpty()
        }
}