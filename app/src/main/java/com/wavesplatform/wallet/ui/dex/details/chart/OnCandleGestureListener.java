package com.wavesplatform.wallet.ui.dex.details.chart;

import android.view.MotionEvent;

import com.github.mikephil.charting.listener.OnChartGestureListener;

public interface OnCandleGestureListener extends OnChartGestureListener {
    void onChartUp(MotionEvent me);
}
