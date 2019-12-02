package com.lenta.bp16.model

enum class TaskStatus {
    COMMON,
    PACKING,
    STARTED,
    SELF_LOCK,
    LOCK
}

enum class TaskType {
    PROCESSING_UNIT,
    EXTERNAL_SUPPLY
}

enum class Tabs(val page: Int) {
    PROCESSING(0),
    PROCESSED(1)
}