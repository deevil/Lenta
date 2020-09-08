package com.lenta.bp16.model

enum class IngredientStatusWork {
    IS_PLAY,
    IS_DONE,
    COMMON
}

enum class GoodTypeIcon{
    IS_PLAN,
    IS_FACT,
    IS_VET
}

enum class IngredientStatusBlock{
    SELF_LOCK,
    LOCK,
    COMMON
}

enum class TaskStatus {
    COMMON,
    STARTED,
    SELF_LOCK,
    LOCK
}

enum class ProducerDataStatus{
    VISIBLE,
    GONE,
    ALERT
}

enum class TaskType(val abbreviation: String, val numberLength: Int) {
    WAREHOUSE_INGREDIENTS("ИН", 30),
    PROCESSING_UNIT("ЕО", 20),
    EXTERNAL_SUPPLY("ВП", 10)
}

enum class Tabs(val page: Int) {
    PROCESSING(0),
    PROCESSED(1)
}

enum class SearchStatus {
    DUALISM,
    FOUND_ORDER,
    FOUND_MATERIAL,
    NOT_FOUND
}