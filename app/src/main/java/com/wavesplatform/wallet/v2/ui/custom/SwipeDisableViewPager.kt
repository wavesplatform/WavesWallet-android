package com.wavesplatform.wallet.v2.ui.custom

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

class SwipeDisableViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {

    private var pagingEnabled: Boolean = true

    init {
        this.pagingEnabled = true
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