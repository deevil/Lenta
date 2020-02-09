package com.lenta.shared.utilities.databinding

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.models.core.Uom
import com.lenta.shared.models.core.isOnlyInt
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.enable
import com.redmadrobot.inputmask.MaskedTextChangedListener

@BindingAdapter(value = ["onOkInSoftKeyboard"])
fun setOnOkInSoftKeyboardListener(editText: EditText, onOkInSoftKeyboardListener: OnOkInSoftKeyboardListener?) {
    if (onOkInSoftKeyboardListener == null) {
        editText.setOnEditorActionListener(null)
        return
    }
    editText.setOnEditorActionListener { view: TextView?, _: Int?, _: KeyEvent? ->
        view?.let {
            it.clearFocus()
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        onOkInSoftKeyboardListener.onOkInSoftKeyboard()
    }
}

@BindingAdapter(value = ["textAllCaps"])
fun setTextAllCaps(editText: EditText, textAllCaps: Boolean?) {
    if (textAllCaps == true) {
        editText.filters = editText.filters + InputFilter.AllCaps()
    } else {
        editText.filters = editText.filters.filter { it != InputFilter.AllCaps() }.toTypedArray()
    }
}

@BindingAdapter(value = ["digitsForUom"])
fun setDigitsForUom(editText: EditText, uom: Uom?) {
    uom?.let {
        editText.keyListener = DigitsKeyListener.getInstance(if (uom.isOnlyInt()) "0123456789-" else "0123456789.-")
    }
}

@BindingAdapter("requestFocus", "cursorToLastPos")
fun requestFocus(editText: EditText, @Suppress("UNUSED_PARAMETER") requestFocus: Any?, cursorToLastPos: Boolean?) {
    requestFocus?.let {
        editText.requestFocus()
        if (cursorToLastPos == true) {
            editText.setSelection(editText.text.length)
        }
    }
}

@BindingAdapter("connectFocusWith")
fun connectFocusWith(editText: EditText, focusState: MutableLiveData<Boolean>) {
    editText.setOnFocusChangeListener { _, hasFocus -> focusState.value = hasFocus }
}

@BindingAdapter("selectText")
fun selectText(editText: EditText, isSelect: Boolean?) {
    if (editText.isEnabled && isSelect == true) {
        editText.requestFocus()
        editText.setSelection(0, editText.text.length)
    }
}

@BindingAdapter(value = ["disabled"])
fun setDisabled(editText: EditText, disabled: Boolean?) {
    if (disabled == null) {
        editText.enable(disabled == false)
    }
}

@BindingAdapter("maskPattern")
fun setMaskPattern(editText: EditText, mask: String) {
    val listener = MaskedTextChangedListener(mask, editText)
    editText.addTextChangedListener(listener)
    editText.onFocusChangeListener = listener
}

@BindingAdapter("maxValue")
fun setMaxValue(editText: EditText, value: Int) {
    editText.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            Logg.d { "--> entered value: $s (max value: $value)" }
            var enteredValue = s.toString()
            if (enteredValue.toIntOrNull() ?: 0 > value) {
                enteredValue = enteredValue.dropLast(1)
                editText.setText(enteredValue)
                editText.requestFocus()
                editText.setSelection(enteredValue.length)
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    })
}

@BindingAdapter("saveFocusTo")
fun saveFocusTo(editText: EditText, lastFocusField: MutableLiveData<EditText?>) {
    editText.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) lastFocusField.value = editText }
}

interface OnOkInSoftKeyboardListener {
    fun onOkInSoftKeyboard(): Boolean
}