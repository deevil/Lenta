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
        RectColor.GREEN.getPaint(context)
    }

    val paintRed by lazy {
        RectColor.RED.getPaint(context)
    }

    val paintYelow by lazy {
        RectColor.YELLOW.getPaint(context)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let { canvas1 ->
            rects.forEach {
                val paint = if (it.error == true) paintRed else if (it.error == false) paintGreen else paintYelow
                canvas1.drawRect(it.rect, paint)
                if (it.text.isNotEmpty()) {
                    canvas1.drawText(it.text, it.rect.left.toFloat(), it.rect.top.toFloat() - 12, paint)
                }
            }
        }
    }


    fun cleanAllRects() {
        rects.clear()
    }

    fun addRectInfo(
            rect: Rect,
            error: Boolean?,
            text: String = ""
    ) {
        rects.add(RectInfo(rect, error, text))

    }


}

private fun RectColor.getPaint(context: Context): Paint {
    return Paint().apply {
        color = ContextCompat.getColor(context, getColorRes())
        strokeWidth = 4.px
        style = Paint.Style.STROKE
        textSize = 24.px
    }
}

data class RectInfo(
        val rect: Rect,
        val error: Boolean?,
        val text: String
)

enum class RectColor {
    GREEN, RED, YELLOW
}

private fun RectColor.getColorRes(): Int {
    return when (this) {
        RectColor.GREEN -> R.color.color_normal_green
        RectColor.RED -> R.color.color_normal_red
        RectColor.YELLOW -> R.color.color_normal_yellow
    }
}