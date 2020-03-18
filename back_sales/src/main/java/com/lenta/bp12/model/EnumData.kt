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

enum class GoodType {
    COMMON,
    ALCOHOL,
    EXCISE
}

enum class ControlType(val code: String) {
    UNKNOWN("UNKNOWN"),
    COMMON("N"),
    ALCOHOL("A")
}