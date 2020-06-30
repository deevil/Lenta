package com.lenta.shared.utilities.extentions

import com.lenta.shared.keys.KeyCode
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler

fun RecyclerViewKeyHandler<*>.processItemClickHandler(position: Int) {
    if (this.isSelected(position)) {
        customKeyHandler?.invoke(position)
    } else {
        this.selectPosition(position)
    }
}

fun RecyclerViewKeyHandler<*>.onFragmentKeyDownHandler(keyCode: KeyCode): Boolean {
    var returnValue = false
    if (!this.onKeyDown(keyCode)) {
        if (keyCode.keyCode == KeyCode.KEYCODE_ENTER.keyCode) {
            this.posInfo.value?.currentPos?.let { position ->
                customKeyHandler?.invoke(position)
                returnValue = true
            }
        }
    }

    return returnValue
}