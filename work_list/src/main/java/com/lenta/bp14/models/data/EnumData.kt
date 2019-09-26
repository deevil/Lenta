package com.lenta.bp14.models.data

import com.lenta.bp14.R

enum class GoodsListTab(val position: Int) {
    PROCESSING(0),
    PROCESSED(1),
    SEARCH(2)
}

enum class TaskListTab(val position: Int) {
    PROCESSING(0),
    SEARCH(1)
}

enum class GoodDetailsTab(val position: Int) {
    SHELF_LIVES(0),
    COMMENTS(1)
}

enum class ShelfLifeType(val position: Int) {
    BEFORE(0),
    PRODUCED(1)
}

enum class GoodType {
    COMMON,
    ALCOHOL,
    MARKED
}

fun GoodType.getDescriptionResId(): Int {
    return when (this) {
        GoodType.COMMON -> R.string.common_product
        GoodType.ALCOHOL -> R.string.alcohol
        GoodType.MARKED -> R.string.marked_product
    }
}