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

enum class QuantityType {
    QUANTITY,
    CONSIGNMENT,
    MARK
}

enum class GoodType {
    COMMON,
    ALCOHOL,
    MARK
}