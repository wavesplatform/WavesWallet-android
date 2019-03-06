package com.wavesplatform.wallet.v2.ui.custom

import android.graphics.Matrix
import android.view.MotionEvent
import android.view.View

import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet
import com.github.mikephil.charting.listener.BarLineChartTouchListener

class CandleTouchListener
/**
 * Constructor with initialization parameters.
 *
 * @param chart instance of the chart
 * @param touchMatrix the touch-matrix of the chart
 * @param dragTriggerDistance the minimum movement distance that will be interpreted as a "drag" gesture in dp (3dp equals
 */
(chart: BarLineChartBase<out BarLineScatterCandleBubbleData<out IBarLineScatterCandleBubbleDataSet<out Entry>>>, touchMatrix: Matrix, dragTriggerDistance: Float) : BarLineChartTouchListener(chart, touchMatrix, dragTriggerDistance) {

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        val res = super.onTouch(v, event)
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_UP -> (mChart.onChartGestureListener as OnCandleGestureListener).onChartUp(event)
        }
        return res
    }
}
