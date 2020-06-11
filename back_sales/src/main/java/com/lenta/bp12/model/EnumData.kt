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

enum class ScanNumberType(val description: String){
    DEFAULT("Состояние до сканирование первого номера"),
    COMMON("Обычный товар"),
    ALCOHOL("Алкогольный немаркированный товар"),
    MARK_150("Марка 150 символов"),
    MARK_68("Марка 68 символов"),
    PART("Партия"),
    BOX("Коробка")
}
