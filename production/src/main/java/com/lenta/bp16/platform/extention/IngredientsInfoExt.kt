package com.lenta.bp16.platform.extention

import com.lenta.bp16.model.IngredientStatusBlock
import com.lenta.bp16.model.IngredientStatusWork
import com.lenta.bp16.model.ingredients.ui.IngredientInfoUI

fun IngredientInfoUI.isBlockedByMyself(): Boolean = blockType == IngredientInfoUI.BLOCK_BY_MYSELF
fun IngredientInfoUI.isBlockedByAnother(): Boolean = blockType == IngredientInfoUI.BLOCK_BY_OTHER

fun IngredientInfoUI.isBlocked(): Boolean {
        return isBlockedByMyself() || isBlockedByAnother()
}

fun IngredientInfoUI.getModeType(): String {
    return if (isByOrder) {
        if (isBlocked()) IngredientInfoUI.MODE_ORDER_RE_BLOCK_DATA else IngredientInfoUI.MODE_ORDER_BLOCK_DATA
    } else {
        if (isBlocked()) IngredientInfoUI.MODE_MATERIAL_RE_BLOCK_DATA else IngredientInfoUI.MODE_MATERIAL_BLOCK_DATA
    }

}

fun IngredientInfoUI.getIngredientStatusBlock(): IngredientStatusBlock {
    return when (blockType) {
        IngredientInfoUI.BLOCK_BY_MYSELF -> IngredientStatusBlock.SELF_LOCK
        IngredientInfoUI.BLOCK_BY_OTHER -> IngredientStatusBlock.LOCK
        else -> IngredientStatusBlock.COMMON
    }
}

fun IngredientInfoUI.getIngredientStatusWork(): IngredientStatusWork {
    return when {
        isPlay == true -> IngredientStatusWork.IS_PLAY
        isDone == true -> IngredientStatusWork.IS_DONE
        else -> IngredientStatusWork.COMMON
    }
}