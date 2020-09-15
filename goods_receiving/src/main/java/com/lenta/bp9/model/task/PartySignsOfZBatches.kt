package com.lenta.bp9.model.task

import com.lenta.shared.models.core.Uom

//партионные признаки для Z-партий
data class PartySignsOfZBatches(
        val materialNumber: String,
        val batchNumber: String, //Номер партии
        val processingUnit: String, //Номер ЕО
        val typeDiscrepancies: String, //Тип расхождения
        val manufactureCode: String, //ЕГАИС Код организации/производителя
        val shelfLifeDate: String, //Срок годности до (Дата)
        val shelfLifeTime: String, //Срок годности до (время)
        val partySign: PartySignsTypeOfZBatches //партионный признак (ДП-дата производства, СГ-срок годности),
) {
}