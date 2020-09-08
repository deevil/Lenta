package com.lenta.bp9.features.goods_information.base

import com.lenta.bp9.platform.TypeDiscrepanciesConstants

interface IBaseCurrentTypeDiscrepancies : IBaseVariables, IBaseQualityInfo, IBaseReasonRejectionInfo {
    val currentTypeDiscrepanciesCode: String
        get() {
            return currentQualityInfoCode
                    .takeIf { it == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM }
                    ?: currentReasonRejectionInfoCode
        }
}