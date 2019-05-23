package com.lenta.shared.keys


import android.view.KeyEvent

class KeyCodeDetector {

    fun detectKeyCode(event: KeyEvent?): KeyCode {
        if (event == null) {
            return KeyCode.KEYCODE_UNKNOWN
        }

        val keyCodeName = event.characters ?: return KeyCode.KEYCODE_UNKNOWN

        val keyCode: KeyCode
        try {
            keyCode = KeyCode.valueOf(keyCodeName)
        } catch (e: IllegalArgumentException) {
            return KeyCode.KEYCODE_UNKNOWN
        }

        return keyCode
    }

}
