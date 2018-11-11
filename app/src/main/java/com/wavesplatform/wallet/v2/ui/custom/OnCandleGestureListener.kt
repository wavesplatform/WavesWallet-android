package com.wavesplatform.wallet.v2.ui.custom

import android.view.MotionEvent

import com.github.mikephil.charting.listener.OnChartGestureListener

interface OnCandleGestureListener : OnChartGestureListener {
    fun onChartUp(me: MotionEvent)
}
