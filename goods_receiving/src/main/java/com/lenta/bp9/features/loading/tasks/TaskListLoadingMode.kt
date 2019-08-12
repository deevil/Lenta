package com.lenta.bp9.features.loading.tasks

enum class TaskListLoadingMode(val taskListLoadingModeString: String) {
    None(""),
    Receiving("1"), //Приемка
    PGE("2"), //ПГЕ
    Shipment("3") //Отгрузка
}