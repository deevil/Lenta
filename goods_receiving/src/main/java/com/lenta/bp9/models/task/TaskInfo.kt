package com.lenta.bp9.models.task

class TaskInfo(val position: String, //Номер по порядку (отформатирован пробелами для показа)
               val taskNumber: String, //Номер задания по приемке
               val topText: String, //Текст длиной 40 знаков (Используется в первой строке на экране Список заданий)
               val bottomText: String, //Текст длиной 40 знаков (Используется в второй строке на экране Список заданий)
               val caption: String, //Текст длиной 80 знаков (Используется для заголовка задания на экране «карточка задания»)
               val taskType: TaskType, //Тип задания
               val positionsCount: Int, //Количество позиций
               val status: TaskStatus, //Текущий статус задания
               val isStarted: Boolean, //Признак – Задание начато но не закончено
               val lockStatus: TaskLockStatus, //Признак – Задание заблокировано
               val isDelayed: Boolean, //Признак – Поставщик задерживается (отображаем задание на закладке отложенные)
               val isPaused: Boolean, //Признак – Ожидание (отображаем задание на закладке отложенные)
               val isCracked: Boolean, //Признак – Задание взлом (Используется для ПГЕ)
               val documentNumber: String, //Номер документа закупки
               val transportationOTM: String) { //Транспортировка ОТМ

    fun matchesFilter(filter: String): Boolean {
        return taskNumber.contains(filter, true) ||
                documentNumber.contains(filter, true) ||
                transportationOTM.contains(filter, true)
    }
}