package com.lenta.bp9.features.goods_information.base

interface IBaseSpinQualityInfo : IBaseVariables {
    val currentQualityInfoCode: String
        get() {
            val position = spinQualitySelectedPosition.value ?: -1
            return position
                    .takeIf { it >= 0 }
                    ?.let {
                        qualityInfo.value
                                ?.getOrNull(it)
                                ?.code
                                .orEmpty()
                    }.orEmpty()
        }
}