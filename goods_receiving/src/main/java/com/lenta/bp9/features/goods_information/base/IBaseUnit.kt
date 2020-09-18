package com.lenta.bp9.features.goods_information.base

import com.lenta.bp9.features.base.IBaseTaskManager
import com.lenta.bp9.model.task.TaskType
import com.lenta.shared.models.core.Uom

interface IBaseUnit : IBaseVariables, IBaseTaskManager {
    val unitByTaskType: Uom?
        get() =
            if (taskType == TaskType.DirectSupplier) {
                productInfo.value?.purchaseOrderUnits
            } else {
                productInfo.value?.uom
            }

    val unitNameByTaskType: String
        get() =
            if (taskType == TaskType.DirectSupplier) {
                productInfo.value?.purchaseOrderUnits?.name.orEmpty()
            } else {
                productInfo.value?.uom?.name.orEmpty()
            }

    val unitCodeByTaskType: String
        get() =
            if (taskType == TaskType.DirectSupplier) {
                productInfo.value?.purchaseOrderUnits?.code.orEmpty()
            } else {
                productInfo.value?.uom?.code.orEmpty()
            }
}