package com.lenta.bp14.features.price_check.price_scanner

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class MyCanvas @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val rects = mutableListOf<RectInfo>()

    val paint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 4.0F
        style = Paint.Style.STROKE
        textSize = 24F
    }

    val paintRed = Paint().apply {
        color = Color.RED
        strokeWidth = 4.0F
        style = Paint.Style.STROKE
        textSize = 24F
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let { canvas1 ->
            rects.forEach {
                val paint = if (it.error) paintRed else paint
                canvas1.drawRect(it.rect, paint)
                canvas1.drawText(it.text, it.rect.left.toFloat(), it.rect.top.toFloat() - 12, paint)
            }


        }
    }


    fun cleanAllRects() {
        rects.clear()
    }

    fun addRectInfo(
        rect: Rect,
        error: Boolean,
        text: String
    ) {
        rects.add(RectInfo(rect, error, text))

    }


}

data class RectInfo(
    val rect: Rect,
    val error: Boolean,
    val text: String
)