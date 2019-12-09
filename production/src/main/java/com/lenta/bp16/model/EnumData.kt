package com.lenta.bp16.model

enum class TaskStatus {
    COMMON,
    PACKING,
    STARTED,
    SELF_LOCK,
    LOCK
}

enum class TaskType(val abbreviation: String) {
    PROCESSING_UNIT("ЕО"),
    EXTERNAL_SUPPLY("ВП")
}

enum class Tabs(val page: Int) {
    PROCESSING(0),
    PROCESSED(1)
}