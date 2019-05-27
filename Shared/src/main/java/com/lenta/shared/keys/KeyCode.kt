package com.lenta.shared.keys


import android.view.KeyEvent

enum class KeyCode constructor(val keyCode: Int) {
    KEYCODE_UNKNOWN(KeyEvent.KEYCODE_UNKNOWN),
    KEYCODE_ENTER(KeyEvent.KEYCODE_ENTER),
    KEYCODE_ESCAPE(KeyEvent.KEYCODE_ESCAPE),
    KEYCODE_DPAD_DOWN(KeyEvent.KEYCODE_DPAD_DOWN),
    KEYCODE_DPAD_UP(KeyEvent.KEYCODE_DPAD_UP),
    KEYCODE_DPAD_LEFT(KeyEvent.KEYCODE_DPAD_LEFT),
    KEYCODE_DPAD_RIGHT(KeyEvent.KEYCODE_DPAD_RIGHT);


    companion object {

        fun detectKeyCode(keyCode: Int): KeyCode {
            for (keyCodeRes in values()) {
                if (keyCodeRes.keyCode == keyCode) {
                    return keyCodeRes
                }
            }

            return KEYCODE_UNKNOWN
        }
    }
}
