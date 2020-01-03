package com.lenta.bp9.model.task

enum class TaskType(val taskTypeString: String) {
    None(""),
    DirectSupplier("ППП"),
    ReceptionDistributionCenter("ПРЦ"),
    RecalculationCargoUnit("ПГЕ");

    companion object {
        fun from(taskTypeString: String): TaskType {
            return when(taskTypeString) {
                "ППП" -> DirectSupplier
                "ПРЦ" -> ReceptionDistributionCenter
                "ПГЕ" -> RecalculationCargoUnit
                else -> None
            }
        }
    }
}