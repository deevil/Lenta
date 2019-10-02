package com.lenta.bp9.model.task

import com.lenta.shared.models.core.Uom

//Таблица 87 ZSGRZ_TASK_PARTS_EXCH Структура строки таблицы партий задания
data class TaskBatchInfo(
        val materialNumber: String,
        val description: String,
        val uom: Uom,
        val batchNumber: String,
        val alcoСode: String,
        val manufacturer: String,
        val bottlingDate: String,
        val planQuantityBatch: String,
        val isNoEAN: Boolean
) {
    fun getMaterialLastSix(): String {
        return if (materialNumber.length > 6)
            materialNumber.substring(materialNumber.length - 6)
        else
            materialNumber
    }
}