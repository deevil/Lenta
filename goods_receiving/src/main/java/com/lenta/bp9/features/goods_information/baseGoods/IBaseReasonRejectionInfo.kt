package com.lenta.bp9.features.goods_information.baseGoods

interface IBaseReasonRejectionInfo : IBaseVariables {
    val currentReasonRejectionInfoCode: String
        get() {
            val position = spinReasonRejectionSelectedPosition.value ?: -1
            return position
                    .takeIf { it >= 0 }
                    ?.let {
                        reasonRejectionInfo.value
                                ?.getOrNull(it)
                                ?.code
                                .orEmpty()
                    }
                    .orEmpty()
        }
}