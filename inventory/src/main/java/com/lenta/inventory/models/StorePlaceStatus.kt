package com.lenta.inventory.models

enum class StorePlaceStatus(val status: String) {
    None(""),
    Finished("1"),
    LockedByMe("2"),
    LockedByOthers("3"),
    Started("4")
}

//0 - ничего
//1 - Обработано(сохранено) (галочка)
//2 - заблокировано мной (открытый замок)
//3 - заблокировано кем то (закрытый замок)
//4 - начато (треугольник)