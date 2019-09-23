package com.lenta.bp9.model.task

import com.lenta.shared.models.core.Uom

//Таблица 86 ZSGRZ_TASK_DIF_EXCH Структура строки таблицы расхождений по товару
data class TaskProductDiscrepancies(
        val materialNumber: String,
        val exidv: String,
        val numberDiscrepancies: String, //Количество расхождения в ПЕИ
        val uom: Uom,
        val typeDiscrepancies: String, //Тип расхождения
        //val nameDiscrepancies: String, //Наименование расхождения, добавляется из справочника 20
        val isNotEdit: Boolean,
        val isNew: Boolean
)