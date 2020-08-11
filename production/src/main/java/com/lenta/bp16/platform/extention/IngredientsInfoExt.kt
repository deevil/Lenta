package com.lenta.bp16.platform.extention

import com.lenta.bp16.model.IngredientStatus
import com.lenta.bp16.model.ingredients.IngredientInfo

fun IngredientInfo.isBlockedByMyself(): Boolean = blockType == IngredientInfo.BLOCK_BY_MYSELF
fun IngredientInfo.isBlockedByAnother(): Boolean = blockType == IngredientInfo.BLOCK_BY_OTHER

fun IngredientInfo.isBlocked(): Boolean {
        return isBlockedByMyself() || isBlockedByAnother()
}

fun IngredientInfo.getModeType(): String {
    return if (isByOrder) {
        if (isBlocked()) IngredientInfo.MODE_ORDER_RE_BLOCK_DATA else IngredientInfo.MODE_ORDER_BLOCK_DATA
    } else {
        if (isBlocked()) IngredientInfo.MODE_MATERIAL_RE_BLOCK_DATA else IngredientInfo.MODE_MATERIAL_BLOCK_DATA
    }

}

fun IngredientInfo.getIngredientStatus(): IngredientStatus {
    return when {
        blockType == IngredientInfo.BLOCK_BY_MYSELF -> IngredientStatus.SELF_LOCK
        blockType == IngredientInfo.BLOCK_BY_OTHER -> IngredientStatus.LOCK
        isPlay == true -> IngredientStatus.IS_PLAY
        isDone == true -> IngredientStatus.IS_DONE
        else -> IngredientStatus.COMMON
    }
}