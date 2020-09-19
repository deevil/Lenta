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

    val orderUnitName: String
        get() = productInfo.value?.purchaseOrderUnits?.name.orEmpty()

    val orderUnitCode: String
        get() = productInfo.value?.purchaseOrderUnits?.code.orEmpty()

    val baseUnitName: String
        get() = productInfo.value?.uom?.name.orEmpty()

    val baseUnitCode: String
        get() = productInfo.value?.uom?.code.orEmpty()

    fun convertEizToBei() : Double { //todo сменить название на convertOrderUnitToBaseUnit после того, как все ViewModel будут переведены на базовый класс
        var convertCount = countValue.value ?: 0.0
        if (isSelectedOrderUnit.value == true) {
            convertCount *= productInfo.value?.quantityInvest?.toDoubleOrNull() ?: 1.0
        }
        return convertCount
    }
}