package com.lenta.bp9.model

import com.lenta.shared.models.core.Uom

//Таблица 86 ZSGRZ_TASK_DIF_EXCH Структура строки таблицы расхождений по товару
data class ReceivingProductDiscrepancies(
        val materialNumber: String,
        val exidv: String,
        val numberDiscrepancies: String, //Количество расхождения в ПЕИ
        val uom: Uom,
        val typeDifferences: String, //Тип расхождения
        val isNotEdit: Boolean
) {
}