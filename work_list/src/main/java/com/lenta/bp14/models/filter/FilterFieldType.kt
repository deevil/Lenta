package com.lenta.bp14.models.filter

enum class FilterFieldType {
    NUMBER,
    GROUP,
    SECTION,
    PLACE_STORAGE,
    COMMENT
}

data class FilterParameter(
        val filterFieldType: FilterFieldType,
        val value: String
)

