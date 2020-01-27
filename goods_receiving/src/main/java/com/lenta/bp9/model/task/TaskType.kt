package com.lenta.bp9.model.task

enum class TaskType(val taskTypeString: String) {
    None(""),
    DirectSupplier("ППП"),
    ReceptionDistributionCenter("ПРЦ"),
    OwnProduction("ПСП"),
    RecalculationCargoUnit("ПГЕ"),
    ShipmentPP("ОПП"),
    ShipmentRC("ОРЦ"),;

    companion object {
        fun from(taskTypeString: String): TaskType {
            return when(taskTypeString) {
                "ППП" -> DirectSupplier
                "ПРЦ" -> ReceptionDistributionCenter
                "ПСП" -> OwnProduction
                "ПГЕ" -> RecalculationCargoUnit
                "ОПП" -> ShipmentPP
                "ОРЦ" -> ShipmentRC
                else -> None
            }
        }
    }
}