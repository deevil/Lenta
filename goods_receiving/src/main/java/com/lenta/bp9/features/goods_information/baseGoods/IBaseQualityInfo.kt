package com.lenta.bp9.features.goods_information.baseGoods

interface IBaseQualityInfo : IBaseVariables {
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