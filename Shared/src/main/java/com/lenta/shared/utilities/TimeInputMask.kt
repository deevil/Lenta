package com.lenta.shared.utilities

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class TimeInputMask(val input : EditText) {

    fun listen() {
        input.addTextChangedListener(timeEntryWatcher)
    }

    private val timeEntryWatcher = object : TextWatcher {

        var edited = false
        val dividerCharacter = ":"

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (edited) {
                edited = false
                return
            }

            var working = getEditText()

            working = manageTimeDivider(working, start, before)

            edited = true
            input.setText(working)
            input.setSelection(input.text.length)
        }

        private fun manageTimeDivider(working: String, start: Int, before: Int) : String{
            if (working.length == POSITION_DIVIDER) {
                return if (before <= POSITION_DIVIDER && start < POSITION_DIVIDER)
                    working + dividerCharacter
                else
                    working.dropLast(1)
            }
            return working
        }

        private fun getEditText() : String {
            return if (input.text.length >= MAX_INPUT_TEXT_LENGTH)
                input.text.toString().substring(0, MAX_INPUT_TEXT_LENGTH)
            else
                input.text.toString()
        }

        override fun afterTextChanged(s: Editable) {}
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    }

    companion object {
        private const val POSITION_DIVIDER = 2
        private const val MAX_INPUT_TEXT_LENGTH = 5
    }
}