package com.lenta.bp9.features.goods_information.base

interface IBaseSpinProcessingUnits : IBaseVariables {
    val currentProcessingUnitNumber: String
        get() {
            val position = spinProcessingUnitSelectedPosition.value ?: -1
            return position
                    .takeIf { it >= 0 }
                    ?.let {
                        processingUnitsOfProduct.value
                                ?.getOrNull(it)
                                ?.processingUnit
                                .orEmpty()
                    }.orEmpty()
        }
}