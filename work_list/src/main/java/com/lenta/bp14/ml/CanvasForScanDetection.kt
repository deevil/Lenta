package com.lenta.bp14.ml

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.lenta.bp14.R
import com.lenta.shared.utilities.extentions.px

class CanvasForScanDetection @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val rects = mutableListOf<RectInfo>()

    val paintGreen by lazy {
        CheckStatus.VALID.getPaint(context)
    }

    val paintRed by lazy {
        CheckStatus.NOT_VALID.getPaint(context)
    }

    val paintYelow by lazy {
        CheckStatus.UNKNOWN.getPaint(context)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let { canvas1 ->
            rects.forEach {
                when (it.checkStatus) {
                    CheckStatus.VALID -> paintGreen
                    CheckStatus.NOT_VALID -> paintRed
                    CheckStatus.UNKNOWN -> paintYelow
                    else -> null
                }?.apply {
                    canvas1.drawRect(it.rect, this)
                    if (it.text.isNotEmpty()) {
                        canvas1.drawText(it.text, it.rect.left.toFloat(), it.rect.top.toFloat() - 12, this)
                    }
                }
            }
        }
    }


    fun cleanAllRects() {
        rects.clear()
    }

    fun addRectInfo(
            rect: Rect,
            checkStatus: CheckStatus?,
            text: String = ""
    ) {
        rects.add(RectInfo(rect, checkStatus, text))
    }


}

private fun CheckStatus.getPaint(context: Context): Paint {
    return Paint().apply {
        color = ContextCompat.getColor(context, getColorRes())
        strokeWidth = 4.px
        style = Paint.Style.STROKE
        textSize = 24.px
    }
}

private fun CheckStatus.getColorRes(): Int {
    return when (this) {
        CheckStatus.VALID -> R.color.color_normal_green
        CheckStatus.NOT_VALID -> R.color.color_normal_red
        CheckStatus.UNKNOWN -> R.color.color_normal_yellow
    }
}

data class RectInfo(
        val rect: Rect,
        val checkStatus: CheckStatus?,
        val text: String
)

enum class CheckStatus {
    VALID, NOT_VALID, UNKNOWN
}

