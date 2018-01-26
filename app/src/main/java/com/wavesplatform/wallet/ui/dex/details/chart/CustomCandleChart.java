package com.wavesplatform.wallet.ui.dex.details.chart;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.github.mikephil.charting.charts.CandleStickChart;

public class CustomCandleChart extends CandleStickChart {

    private ViewPager view;

    public CustomCandleChart(Context context) {
        super(context);
    }

    public CustomCandleChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomCandleChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                view.requestDisallowInterceptTouchEvent(true);
                break;

            case MotionEvent.ACTION_UP:
                view.requestDisallowInterceptTouchEvent(false);
                break;
        }

        super.onTouchEvent(ev);
        return true;
    }

    public void setView(ViewPager view) {
        this.view = view;
    }
}
