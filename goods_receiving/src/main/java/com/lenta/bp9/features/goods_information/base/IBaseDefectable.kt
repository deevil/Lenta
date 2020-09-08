package com.lenta.bp9.features.goods_information.base

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.features.base.IBaseTaskManager
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map

interface IBaseDefectable : IBaseVariables, IBaseQualityInfo, IBaseTaskManager {
    val isDefect: MutableLiveData<Boolean>
        get() = spinQualitySelectedPosition.map {
            if (taskType != TaskType.RecalculationCargoUnit) {
                currentQualityInfoCode != TYPE_DISCREPANCIES_QUALITY_NORM
            } else {
                currentQualityInfoCode != TYPE_DISCREPANCIES_QUALITY_NORM
                        && currentQualityInfoCode != TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS
            }
        }
}