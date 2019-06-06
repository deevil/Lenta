package com.lenta.shared.keys


import android.view.KeyEvent
import com.lenta.shared.utilities.Logg

enum class KeyCode constructor(val keyCode: Int, val digit: Int? = null) {
    KEYCODE_UNKNOWN(KeyEvent.KEYCODE_UNKNOWN),
    KEYCODE_ENTER(KeyEvent.KEYCODE_ENTER),
    KEYCODE_ESCAPE(KeyEvent.KEYCODE_ESCAPE),
    KEYCODE_DPAD_DOWN(KeyEvent.KEYCODE_DPAD_DOWN),
    KEYCODE_DPAD_UP(KeyEvent.KEYCODE_DPAD_UP),
    KEYCODE_DPAD_LEFT(KeyEvent.KEYCODE_DPAD_LEFT),
    KEYCODE_DPAD_RIGHT(KeyEvent.KEYCODE_DPAD_RIGHT),
    KEYCODE_0(KeyEvent.KEYCODE_0, digit = 0),
    KEYCODE_1(KeyEvent.KEYCODE_1, digit = 1),
    KEYCODE_2(KeyEvent.KEYCODE_2, digit = 2),
    KEYCODE_3(KeyEvent.KEYCODE_3, digit = 3),
    KEYCODE_4(KeyEvent.KEYCODE_4, digit = 4),
    KEYCODE_5(KeyEvent.KEYCODE_5, digit = 5),
    KEYCODE_6(KeyEvent.KEYCODE_6, digit = 6),
    KEYCODE_7(KeyEvent.KEYCODE_7, digit = 7),
    KEYCODE_8(KeyEvent.KEYCODE_8, digit = 8),
    KEYCODE_9(KeyEvent.KEYCODE_9, digit = 9)
    ;


    companion object {

        fun detectKeyCode(keyCode: Int): KeyCode {
            Logg.d { "keycode: $keyCode" }
            for (keyCodeRes in values()) {
                if (keyCodeRes.keyCode == keyCode) {
                    return keyCodeRes
                }
            }

            return KEYCODE_UNKNOWN
        }
    }
}
