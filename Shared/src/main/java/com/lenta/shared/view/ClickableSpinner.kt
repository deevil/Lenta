package com.lenta.shared.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

import androidx.appcompat.widget.AppCompatSpinner


class ClickableSpinner : AppCompatSpinner {
    private var startClickTime: Long = 0
    private var onClickListener: OnClickListener? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, mode: Int) : super(context, mode) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, mode: Int) : super(context, attrs, defStyleAttr, mode) {}


    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return true
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startClickTime = System.currentTimeMillis()
                return true
            }
            MotionEvent.ACTION_UP -> {
                val clickDuration = System.currentTimeMillis() - startClickTime
                if (clickDuration < MAX_CLICK_DURATION) {
                    event.action = MotionEvent.ACTION_DOWN
                    performClick()
                    if (onClickListener != null) {
                        onClickListener!!.onClick(this)
                    }
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun setOnClickListener(onClickListener: OnClickListener?) {
        this.onClickListener = onClickListener
    }

    companion object {
        private val MAX_CLICK_DURATION = 200
    }
}
