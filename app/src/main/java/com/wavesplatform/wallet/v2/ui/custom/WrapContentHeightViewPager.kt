package com.wavesplatform.wallet.v2.ui.custom

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class WrapContentHeightViewPager : ViewPager {

    private var pagingEnabled: Boolean = true

    init {
        this.pagingEnabled = true
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec

        var height = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child.measure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
            val h = child.measuredHeight
            if (h > height) height = h
        }

        heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (this.pagingEnabled) {
            try {
                return super.onTouchEvent(event)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (this.pagingEnabled) {
            try {
                return super.onInterceptTouchEvent(event)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return false
    }

    fun setPagingEnabled(enabled: Boolean) {
        this.pagingEnabled = enabled
    }
}