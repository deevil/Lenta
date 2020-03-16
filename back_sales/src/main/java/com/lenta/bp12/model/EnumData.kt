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

enum class QuantityType(description: String) {
    QUANTITY("Количество"),
    CONSIGNMENT("Партионно"),
    MARK("Марочно")
}

enum class GoodType {
    COMMON,
    ALCOHOL,
    EXCISE
}