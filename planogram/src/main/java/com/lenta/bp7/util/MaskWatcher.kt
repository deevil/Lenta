package com.lenta.bp7.util

import android.text.TextWatcher
import android.text.Editable
import com.lenta.shared.utilities.Logg


class MaskWatcher(
        private var mask: String
) : TextWatcher {

    private var isRunning = false
    private var isDeleting = false

    override fun afterTextChanged(s: Editable?) {
        if (isRunning || isDeleting) {
            return
        }
        Logg.d { "afterTextChanged started..." }
        isRunning = true

        val editableLength = s?.length ?: 0

        if (editableLength < mask.length) {
            if (mask[editableLength] != '#') {
                s?.append(mask[editableLength])
            } else if (mask[editableLength - 1] != '#') {
                s?.insert(editableLength - 1, mask, editableLength - 1, editableLength)
            }
        }

        isRunning = false
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        isDeleting = count > after
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }
}