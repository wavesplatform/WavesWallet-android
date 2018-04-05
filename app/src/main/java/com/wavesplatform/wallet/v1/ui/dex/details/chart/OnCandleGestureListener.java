package com.wavesplatform.wallet.v1.ui.dex.details.chart;

import android.view.MotionEvent;

import com.github.mikephil.charting.listener.OnChartGestureListener;

public interface OnCandleGestureListener extends OnChartGestureListener {
    void onChartUp(MotionEvent me);
}
