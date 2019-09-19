package com.lenta.bp9.model.task

//Таблица 87 ZSGRZ_TASK_PARTS_EXCH Структура строки таблицы партий задания
data class TaskBatches(
        val materialNumber: String,
        val batchNumber: String,
        val alcoСode: String,
        val manufacturer: String,
        val bottlingDate: String,
        val planQuantityИatch: String
) {
}