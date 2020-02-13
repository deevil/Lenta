package com.lenta.bp16.model

enum class TaskStatus {
    COMMON,
    STARTED,
    SELF_LOCK,
    LOCK
}

enum class TaskType(val abbreviation: String, val numberLength: Int) {
    PROCESSING_UNIT("ЕО", 20),
    EXTERNAL_SUPPLY("ВП", 10)
}

enum class Tabs(val page: Int) {
    PROCESSING(0),
    PROCESSED(1)
}