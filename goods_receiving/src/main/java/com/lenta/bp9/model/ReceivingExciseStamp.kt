package com.lenta.bp9.model

import com.lenta.shared.models.core.ExciseStamp

class ReceivingExciseStamp(materialNumber: String,
                           code: String,
                           val setMaterialNumber: String = "", //материал набора
                           val manufacturerCode: String = "",  //ЕГАИС Код организации
                           val bottlingDate: String = "",      //УТЗ ТСД: Дата розлива
                           val isBadStamp: Boolean = false) : ExciseStamp(materialNumber, code) {
}