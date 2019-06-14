package com.lenta.shared.utilities.extentions

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

fun EditText.showKeyboard() {
    val imm = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_FORCED)
}


fun EditText.disable() {
    isFocusable = false
    isEnabled = false
    isFocusableInTouchMode = false
    isCursorVisible = false
    isClickable = false
    isActivated = false
    isCursorVisible = false
    setSelectAllOnFocus(false)
    setTextIsSelectable(false)
    keyListener = null
    clearFocus()
}
