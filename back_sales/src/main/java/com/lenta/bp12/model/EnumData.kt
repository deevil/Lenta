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