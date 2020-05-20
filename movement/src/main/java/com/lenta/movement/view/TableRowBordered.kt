package com.lenta.movement.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.TableLayout
import android.widget.TableRow
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.view.forEachIndexed
import androidx.core.view.isVisible
import com.lenta.movement.R

class TableRowBordered : TableRow {

    private val paint: Paint = Paint()

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setWillNotDraw(false)

        paint.strokeWidth = resources.getDimensionPixelOffset(R.dimen.divider_stroke_width).toFloat()
        paint.color = ResourcesCompat.getColor(resources, R.color.color_divider, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (measuredHeight <= 0 || measuredWidth <= 0) return

        // top border
        canvas.drawLine(
            0F,
            (paint.strokeWidth / 2),
            measuredWidth.toFloat(),
            (paint.strokeWidth / 2),
            paint
        )

        // vertical divider for each column
        if (childCount > 1) {
            forEachIndexed { index, view ->
                // skip last
                if (index == childCount - 1) return@forEachIndexed

                val horizontalMargin = (view.layoutParams as LayoutParams).marginStart + (view.layoutParams as LayoutParams).marginEnd

                canvas.drawLine(
                    (view.measuredWidth + horizontalMargin).toFloat(),
                    0F,
                    (view.measuredWidth + horizontalMargin).toFloat(),
                    measuredHeight.toFloat(),
                    paint
                )
            }
        }
        val tableLayout = parent as TableLayout

        val isAllNextViewInvisible = tableLayout.children
            .drop(tableLayout.indexOfChild(this))
            .all { it.isVisible.not() }

        val isThisRowLast = tableLayout.indexOfChild(this) == tableLayout.childCount - 1

        if (isThisRowLast || isAllNextViewInvisible) {
            // bottom border
            canvas.drawLine(
                0F,
                measuredHeight.toFloat() - (paint.strokeWidth / 2),
                measuredWidth.toFloat(),
                measuredHeight.toFloat() - (paint.strokeWidth / 2),
                paint
            )
        }
    }
}