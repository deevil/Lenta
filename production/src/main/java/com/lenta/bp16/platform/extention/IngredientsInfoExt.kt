package com.lenta.bp16.platform.extention

import com.lenta.bp16.model.IngredientStatusBlock
import com.lenta.bp16.model.IngredientStatusWork
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

fun IngredientInfo.getIngredientStatusBlock(): IngredientStatusBlock {
    return when (blockType) {
        IngredientInfo.BLOCK_BY_MYSELF -> IngredientStatusBlock.SELF_LOCK
        IngredientInfo.BLOCK_BY_OTHER -> IngredientStatusBlock.LOCK
        else -> IngredientStatusBlock.COMMON
    }
}

fun IngredientInfo.getIngredientStatusWork(): IngredientStatusWork {
    return when {
        isPlay == true -> IngredientStatusWork.IS_PLAY
        isDone == true -> IngredientStatusWork.IS_DONE
        else -> IngredientStatusWork.COMMON
    }
}