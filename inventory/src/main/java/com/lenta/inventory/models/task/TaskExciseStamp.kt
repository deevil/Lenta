package com.lenta.inventory.models.task

import com.lenta.shared.models.core.ExciseStamp

class TaskExciseStamp(materialNumber: String,
                      code: String,
                      val placeCode: String,
                      val boxNumber: String = "",
                      val setMaterialNumber: String = "",
                      val manufacturerCode: String = "",
                      val bottlingDate: String = "",
                      val isBadStamp: Boolean = false) : ExciseStamp(materialNumber, code) {
    //placeCode - Код места хранения
    //boxNumber - Номер коробки
    //setMaterialNumber - материал набора
    //manufacturerCode - ЕГАИС Код организации
    //bottMark - УТЗ ТСД: Дата розлива
    //isBadStamp - признак "плохой" марки
}