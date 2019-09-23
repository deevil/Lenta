package com.lenta.bp9.model.task

import com.lenta.shared.models.core.Uom

//Таблица 88 ZSGRZ_PARTS_DIF_EXCH Структура строки таблицы расхождений по партиям
data class TaskBatchesDiscrepancies(
        val materialNumber: String,
        val batchNumber: String,
        val numberDiscrepancies: String, //Количество расхождения в ПЕИ
        val uom: Uom,
        val typeDiscrepancies: String, //Тип расхождения
        val isNotEdit: Boolean,
        val exciseStampCode: String,
        val fullDM: String //DM акцизной марки
) {
}