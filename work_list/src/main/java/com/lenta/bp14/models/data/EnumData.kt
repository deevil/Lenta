package com.lenta.bp14.models.data

import com.lenta.bp14.R
import com.lenta.bp14.requests.pojo.ProductInfo
import com.lenta.shared.utilities.extentions.isSapTrue

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
    PRODUCTION(0),
    EXPIRATION(1)
}

enum class GoodType {
    COMMON,
    ALCOHOL,
    MARKED
}

fun getGoodType(alcohol: String, marked: String): GoodType {
    if (alcohol.isSapTrue()) return GoodType.ALCOHOL
    if (marked.isSapTrue()) return GoodType.MARKED
    return GoodType.COMMON
}

fun ProductInfo.getGoodType(): GoodType {
    return when {
        this.isAlco.isSapTrue() -> GoodType.ALCOHOL
        this.isMarked.isSapTrue() -> GoodType.MARKED
        else -> GoodType.COMMON
    }
}

fun GoodType.getDescriptionResId(): Int {
    return when (this) {
        GoodType.COMMON -> R.string.common_product
        GoodType.ALCOHOL -> R.string.alcohol
        GoodType.MARKED -> R.string.marked_product
    }
}