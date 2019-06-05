package com.lenta.shared.utilities.extentions

import android.os.Bundle


val KEY_ARGS_ID_CODE_CONFIRM by lazy { "KEY_ARGS_ID_CODE_CONFIRM" }


fun Bundle.getFragmentResultCode(): Int? {
    return this.getInt(KEY_ARGS_ID_CODE_CONFIRM)
}

fun Bundle.setFragmentResultCode(code: Int) {
    putInt(KEY_ARGS_ID_CODE_CONFIRM, code)
}