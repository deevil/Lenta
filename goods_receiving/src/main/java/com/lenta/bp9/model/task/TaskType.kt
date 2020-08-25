package com.lenta.bp9.model.task

enum class TaskType(val taskTypeString: String) {
    None(""),
    /** ППП*/
    DirectSupplier("ППП"),
    /** ПРЦ*/
    ReceptionDistributionCenter("ПРЦ"),
    /** ПТК*/
    ShoppingMall("ПТК"),
    /** ПСП*/
    OwnProduction("ПСП"),
    /** ПГЕ*/
    RecalculationCargoUnit("ПГЕ"),
    /** ОПП*/
    ShipmentPP("ОПП"),
    /** ОРЦ*/
    ShipmentRC("ОРЦ"),;

    companion object {
        fun from(taskTypeString: String): TaskType {
            return when(taskTypeString) {
                "ППП" -> DirectSupplier
                "ПРЦ" -> ReceptionDistributionCenter
                "ПТК" -> ShoppingMall
                "ПСП" -> OwnProduction
                "ПГЕ" -> RecalculationCargoUnit
                "ОПП" -> ShipmentPP
                "ОРЦ" -> ShipmentRC
                else -> None
            }
        }
    }
}