package com.lenta.bp16.model

enum class TaskType {
    COMMON,
    PACKING,
    STARTED,
    SELF_LOCK,
    LOCK
}

enum class Tabs(val page: Int) {
    PROCESSING(0),
    PROCESSED(1)
}