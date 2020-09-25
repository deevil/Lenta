package com.lenta.movement.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.TableLayout
import android.widget.TableRow
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import com.lenta.movement.R
import com.lenta.movement.platform.extensions.implementationOf

class BorderedTableLayout(context: Context?, attrs: AttributeSet?) : TableLayout(context, attrs) {

    private val paint: Paint = Paint().apply {
        strokeWidth = resources.getDimensionPixelOffset(R.dimen.divider_stroke_width).toFloat()
        color = ResourcesCompat.getColor(resources, R.color.color_divider, null)
    }

    private val rows: Sequence<TableRow>
        get() = children.filterIsInstance<TableRow>()

    private val isTopBorderDisabled: Boolean

    init {
        setWillNotDraw(false)
        val args = context?.obtainStyledAttributes(attrs, R.styleable.BorderedTableLayout)
        isTopBorderDisabled = args?.getBoolean(R.styleable.BorderedTableLayout_disable_top_border,
                false) ?: false
        args?.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // top border
        if (!isTopBorderDisabled) {
            canvas.drawLine(
                    0F,
                    (paint.strokeWidth / HALF_DIVIDER),
                    measuredWidth.toFloat(),
                    (paint.strokeWidth / HALF_DIVIDER),
                    paint
            )
        }

        var sumHeight = 0
        rows.forEach { rowView ->
            if (rowView.childCount > 1) {
                var sumWidth = 0
                rowView.children.toList().dropLast(1).forEach { columnView ->
                    val columnLayoutParams = columnView.layoutParams.implementationOf<TableRow.LayoutParams>()!!
                    val horizontalMargin = columnLayoutParams.marginStart + columnLayoutParams.marginEnd

                    sumWidth += columnView.measuredWidth + horizontalMargin
                    canvas.drawLine(
                            sumWidth.toFloat(),
                            sumHeight.toFloat(),
                            sumWidth.toFloat(),
                            sumHeight.toFloat() + rowView.measuredHeight.toFloat(),
                            paint
                    )
                }
            }

            sumHeight += rowView.measuredHeight
            canvas.drawLine(
                    0F,
                    sumHeight.toFloat() - (paint.strokeWidth / HALF_DIVIDER),
                    measuredWidth.toFloat(),
                    sumHeight.toFloat() - (paint.strokeWidth / HALF_DIVIDER),
                    paint
            )
        }
    }

    companion object {
        private const val HALF_DIVIDER = 2
    }
}