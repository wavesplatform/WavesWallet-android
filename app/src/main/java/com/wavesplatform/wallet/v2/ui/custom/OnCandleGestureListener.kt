/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.custom

import android.view.MotionEvent

import com.github.mikephil.charting.listener.OnChartGestureListener

interface OnCandleGestureListener : OnChartGestureListener {
    fun onChartUp(me: MotionEvent)
}
