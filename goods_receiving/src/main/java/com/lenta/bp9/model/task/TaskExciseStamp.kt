package com.lenta.bp9.model.task

import com.lenta.shared.models.core.ExciseStamp

class TaskExciseStamp(materialNumber: String,
                      code: String,
                      val setMaterialNumber: String = "", //материал набора
                      val manufacturerCode: String = "",  //ЕГАИС Код организации
                      val bottlingDate: String = "",      //УТЗ ТСД: Дата розлива
                      val isBadStamp: Boolean = false) : ExciseStamp(materialNumber, code) {
}