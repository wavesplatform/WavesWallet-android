/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.custom

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

import com.github.mikephil.charting.charts.CandleStickChart

class CustomCandleChart : CandleStickChart {

    private var view: ViewPager? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.action
        when (action) {
            MotionEvent.ACTION_DOWN -> view?.requestDisallowInterceptTouchEvent(true)

            MotionEvent.ACTION_UP -> view?.requestDisallowInterceptTouchEvent(false)
        }

        super.onTouchEvent(ev)
        return true
    }

    fun setView(view: ViewPager) {
        this.view = view
    }

    fun onDestroy() {
        this.view = null
    }
}
