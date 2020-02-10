package com.lenta.bp9.features.loading.tasks

enum class TaskListLoadingMode(val taskListLoadingModeString: String) {
    None(""),
    /** Приемка*/
    Receiving("1"), //Приемка
    /** ПГЕ*/
    PGE("2"), //ПГЕ
    /** Отгрузка*/
    Shipment("3"); //Отгрузка

    companion object {
        fun from(taskListLoadingModeString: String): TaskListLoadingMode {
            return when(taskListLoadingModeString) {
                "1" -> Receiving
                "2" -> PGE
                "3" -> Shipment
                else -> None
            }
        }
    }
}