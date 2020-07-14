package com.lenta.shared.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.databinding.BindingAdapter

class EditTextWithSuffix : AppCompatEditText {
    private var textPaint = TextPaint()
    private var suffix = ""
    private val suffixPadding: Float = 0.toFloat()
    private var latestNormalText: String? = ""
    var maxLengthForScanProtect: Int? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    public override fun onDraw(c: Canvas) {
        super.onDraw(c)
        if (text.isNullOrEmpty()) {
            return
        }
        val suffixXPosition = textPaint.measureText(text!!.toString()).toInt() + paddingLeft
        textPaint.color = currentTextColor
        c.drawText(suffix, Math.max(suffixXPosition.toFloat(), suffixPadding), baseline.toFloat(), textPaint)
    }

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        maxLengthForScanProtect?.let {
            val currentText = text.toString()
            if (currentText.length > it) {
                setText(latestNormalText)
                return
            } else {
                latestNormalText = currentText
            }
        }
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        textPaint.color = currentTextColor
        textPaint.textSize = textSize
        textPaint.textAlign = Paint.Align.LEFT
    }

    fun setSuffix(suffix: String) {
        this.suffix = suffix
    }

}

@BindingAdapter(value = ["suffix"])
fun setSuffix(editText: EditTextWithSuffix, suffix: String?) {
    @Suppress("NAME_SHADOWING") val suffix = suffix.orEmpty()
    editText.setSuffix(" $suffix")
    editText.hint = suffix

}

@BindingAdapter(value = ["maxLengthForScanProtect"])
fun setSuffix(editText: EditTextWithSuffix, maxLengthForScanProtect: Int?) {
    editText.maxLengthForScanProtect = maxLengthForScanProtect
}