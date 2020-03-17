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

enum class QuantityType() {
    QUANTITY,
    PART,
    MARK
}

enum class GoodType {
    COMMON,
    ALCOHOL,
    EXCISE
}