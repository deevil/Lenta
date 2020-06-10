package com.lenta.bp12.model

enum class TaskStatus {
    COMMON,
    STARTED
}

enum class BlockType {
    UNLOCK,
    SELF_LOCK,
    LOCK
}

enum class GoodKind {
    COMMON,
    ALCOHOL,
    EXCISE
}

enum class ControlType(val code: String, val description: String) {
    UNKNOWN("UNKNOWN", "Неизвестный"),
    COMMON("N", "Обычный"),
    ALCOHOL("A", "Алкоголь")
}

enum class Category(val description: String) {
    QUANTITY("Количество"),
    CONSIGNMENT("Партионно"),
    MARK("Марочно")
}

enum class MarkStatus(val code: String){
    OK("01"),
    BAD("02"),
    OTHER("03"),
    UNKNOWN("04")
}