package com.lenta.bp9.features.goods_information.base

import com.lenta.bp9.features.base.IBaseTaskManager
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM

interface IBaseTypeDiscrepanciesByTaskType : IBaseVariables, IBaseTaskManager, IBaseSpinQualityInfo, IBaseSpinReasonRejectionInfo {
    val currentTypeDiscrepanciesCodeByTaskType: String
        get() {
            return if (taskType == TaskType.RecalculationCargoUnit) {
                currentQualityInfoCode
            } else {
                currentQualityInfoCode
                        .takeIf { it == TYPE_DISCREPANCIES_QUALITY_NORM }
                        ?: currentReasonRejectionInfoCode
            }
        }
}