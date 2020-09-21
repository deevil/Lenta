package com.lenta.bp9.features.goods_information.base

interface IBaseTermControl : IBaseVariables {
    val currentTermControlCode: String
        get() {
            val position = spinTermControlSelectedPosition.value ?: -1
            return position
                    .takeIf { it >= 0 }
                    ?.let {
                        termControlType.value
                                ?.getOrNull(it)
                                ?.code
                                .orEmpty()
                    }.orEmpty()
        }
}