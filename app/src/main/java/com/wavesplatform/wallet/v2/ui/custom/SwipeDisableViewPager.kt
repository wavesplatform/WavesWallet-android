/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.custom

import android.content.Context
import androidx.viewpager.widget.ViewPager
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