package com.lenta.shared.view

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent

import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.view.GestureDetectorCompat


class ClickableSpinner : AppCompatSpinner, GestureDetector.OnGestureListener {

    private var onClickListener: OnClickListener? = null
    private lateinit var mDetector: GestureDetectorCompat

    constructor(context: Context) : super(context) {
        initDetector(context)
    }

    constructor(context: Context, mode: Int) : super(context, mode) {
        initDetector(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initDetector(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initDetector(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, mode: Int) : super(context, attrs, defStyleAttr, mode) {
        initDetector(context)
    }

    private fun initDetector(context: Context) {
        mDetector = GestureDetectorCompat(context, this)
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun setOnClickListener(onClickListener: OnClickListener?) {
        this.onClickListener = onClickListener
    }


    override fun onShowPress(p0: MotionEvent?) {

    }

    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        onClickListener?.onClick(this)
        return true
    }

    override fun onDown(p0: MotionEvent?): Boolean {
        return false
    }

    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        return false
    }

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        return false
    }

    override fun onLongPress(p0: MotionEvent?) {

    }

}
