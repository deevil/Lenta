package com.lenta.bp9.model

import com.lenta.shared.models.core.Uom

//Таблица 86 ZSGRZ_TASK_DIF_EXCH Структура строки таблицы расхождений по товару
class ReceivingProductDiscrepancies(
        val materialNumber: String,
        val exidv: String,
        val numberDiscrepancies: String, //Количество расхождения в ПЕИ
        val uom: Uom,
        val typeDifferences: String, //Тип расхождения
        val isNotEdit: Boolean
) {
    fun copy(materialNumber: String = this.materialNumber,
             exidv: String = this.exidv,
             numberDiscrepancies: String = this.numberDiscrepancies,
             uom: Uom = this.uom,
             typeDifferences: String = this.typeDifferences,
             isNotEdit: Boolean = this.isNotEdit) : ReceivingProductDiscrepancies {
        return ReceivingProductDiscrepancies(
                materialNumber = materialNumber,
                exidv = exidv,
                numberDiscrepancies = numberDiscrepancies,
                uom = uom,
                typeDifferences = typeDifferences,
                isNotEdit = isNotEdit
        )
    }
}