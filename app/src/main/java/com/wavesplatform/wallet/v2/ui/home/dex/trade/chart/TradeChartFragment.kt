package com.wavesplatform.wallet.v2.ui.home.dex.trade.chart

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.ui.dex.details.chart.CandleTouchListener
import com.wavesplatform.wallet.v1.ui.dex.details.chart.OnCandleGestureListener
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.TradeActivity
import kotlinx.android.synthetic.main.activity_trade.*
import kotlinx.android.synthetic.main.fragment_trade_chart.*
import kotlinx.android.synthetic.main.layout_empty_data.*
import javax.inject.Inject


class TradeChartFragment : BaseFragment(), TradeChartView, OnCandleGestureListener{

    @Inject
    @InjectPresenter
    lateinit var presenter: TradeChartPresenter

    @ProvidePresenter
    fun providePresenter(): TradeChartPresenter = presenter

//    internal var valueFormatter = { value, axis ->
//        val simpleDateFormat = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
//
//        val date = Date(value.toLong() * 1000 * 60 * currentTimeFrame.toLong())
//        simpleDateFormat.format(date)
//    }

    override fun configLayoutRes() = R.layout.fragment_trade_chart


    override fun onViewReady(savedInstanceState: Bundle?) {
        text_empty.text = getString(R.string.chart_empty)

        setUpChart()
    }

    private fun setUpChart() {
        candle_chart.onTouchListener = CandleTouchListener(candle_chart, candle_chart.getViewPortHandler().getMatrixTouch(), 3f)

        val rightAxis = candle_chart.getAxisRight()
        rightAxis.setDrawGridLines(true)
        rightAxis.setTextColor(Color.parseColor("#808080"))
        rightAxis.setTextSize(8f)
        rightAxis.setDrawAxisLine(false)
        rightAxis.setLabelCount(10, true)
        rightAxis.setMaxWidth(50f)
        rightAxis.setMinWidth(50f)

        val leftAxis = candle_chart.getAxisLeft()
        leftAxis.setEnabled(false)

        val xAxis = candle_chart.getXAxis()
        xAxis.setTextSize(11f)
        xAxis.setGranularityEnabled(true)
        xAxis.setLabelCount(3)

        xAxis.setTextColor(Color.parseColor("#808080"))
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM)
        xAxis.setGranularityEnabled(true)
//        xAxis.setValueFormatter(viewModel.valueFormatter)

        candle_chart.setView((activity as TradeActivity).viewpageer_trade)
        candle_chart.setScaleXEnabled(true)
        candle_chart.setScaleYEnabled(false)
        candle_chart.setAutoScaleMinMaxEnabled(true)
        candle_chart.setVisibleXRange(10.0f, 100.0f)
        candle_chart.setDoubleTapToZoomEnabled(false)
        candle_chart.setPinchZoom(false)

//        viewModel.chartModel.getData().setData(CandleData())

//        candle_chart.getXAxis().setValueFormatter(viewModel.valueFormatter)
        candle_chart.setOnChartGestureListener(this)
        candle_chart.getDescription().setEnabled(false)
        candle_chart.setDrawGridBackground(false)
        candle_chart.getLegend().setEnabled(false)
        candle_chart.setBackgroundColor(Color.parseColor("#fafafa"))
        candle_chart.setExtraBottomOffset(25f)
        candle_chart.setExtraLeftOffset(15f)
        
        bar_chart.setView((activity as TradeActivity).viewpageer_trade)
        bar_chart.onChartGestureListener = BarChartListener()
        bar_chart.setPinchZoom(false)
        bar_chart.setScaleXEnabled(false)
        bar_chart.setScaleYEnabled(false)
        bar_chart.setScaleEnabled(false)
        bar_chart.setDragEnabled(false)
        bar_chart.setVisibleXRange(10.0f, 100.0f)
        bar_chart.setAutoScaleMinMaxEnabled(true)
        //bar_chart.setMinOffset(0.f);
        bar_chart.getDescription().setEnabled(false)
        bar_chart.getLegend().setEnabled(false)
        bar_chart.setExtraLeftOffset(15f)
        bar_chart.setDoubleTapToZoomEnabled(false)

        bar_chart.getAxisLeft().setEnabled(false)

        bar_chart.getAxisRight().setMaxWidth(50f)
        bar_chart.getAxisRight().setMinWidth(50f)
        bar_chart.getAxisRight().setLabelCount(4)
        bar_chart.getAxisRight().setDrawGridLines(true)
        bar_chart.getAxisRight().setTextColor(Color.parseColor("#808080"))
        bar_chart.getAxisRight().setTextSize(8f)
        bar_chart.getAxisRight().setDrawAxisLine(false)
        bar_chart.getAxisRight().setAxisMinimum(0f)
        bar_chart.getAxisRight().setLabelCount(4)

        //bar_chart.getXAxis().setEnabled(false);
        //bar_chart.getXAxis().setDrawGridLines(true);
        //bar_chart.getXAxis().setDrawAxisLine(true);
        bar_chart.getXAxis().setDrawLabels(false)
        bar_chart.getXAxis().setDrawAxisLine(false)
        bar_chart.getXAxis().setGranularityEnabled(true)
        bar_chart.getXAxis().setLabelCount(3)

    }

    inner class BarChartListener : OnChartGestureListener {

        private var prevScaleX = 0f

        override fun onChartGestureStart(me: MotionEvent, lastPerformedGesture: ChartTouchListener.ChartGesture) {

        }

        override fun onChartGestureEnd(me: MotionEvent, lastPerformedGesture: ChartTouchListener.ChartGesture) {
            prevScaleX = 0f
        }

        override fun onChartLongPressed(me: MotionEvent) {

        }

        override fun onChartDoubleTapped(me: MotionEvent) {

        }

        override fun onChartSingleTapped(me: MotionEvent) {

        }

        override fun onChartFling(me1: MotionEvent, me2: MotionEvent, velocityX: Float, velocityY: Float) {

        }

        override fun onChartScale(me: MotionEvent, scaleX: Float, scaleY: Float) {
            val xPos = (me.getX(0) + me.getX(1)) / 2f
            val yPos = (me.getY(0) + me.getY(1)) / 2f
            val z1 = if (prevScaleX > 0) scaleX / prevScaleX else scaleX
            candle_chart.zoom(z1, 1f, xPos, yPos)
            prevScaleX = scaleX
        }

        override fun onChartTranslate(me: MotionEvent, dX: Float, dY: Float) {
            //binding.candleChart.moveViewToX(binding.barChart.getLowestVisibleX());
            //checkInterval();
        }
    }

    override fun onChartGestureStart(me: MotionEvent, lastPerformedGesture: ChartTouchListener.ChartGesture) {}

    override fun onChartGestureEnd(me: MotionEvent, lastPerformedGesture: ChartTouchListener.ChartGesture) {
        prevScaleX = 0f
//        checkInterval()
    }

    override fun onChartUp(me: MotionEvent) {
//        if (!isLoading && !candleChart.isDragEnabled()) {
//            binding.candleChart.setScaleXEnabled(true)
//            candleChart.setDragEnabled(true)
//        }
    }

    override fun onChartLongPressed(me: MotionEvent) {

    }

    override fun onChartDoubleTapped(me: MotionEvent) {
        //checkInterval();
    }

    override fun onChartSingleTapped(me: MotionEvent) {}

    override fun onChartFling(me1: MotionEvent, me2: MotionEvent, velocityX: Float, velocityY: Float) {

    }

    private var prevScaleX = 0f

    override fun onChartScale(me: MotionEvent, scaleX: Float, scaleY: Float) {
        val xPos = (me.getX(0) + me.getX(1)) / 2f
        val yPos = (me.getY(0) + me.getY(1)) / 2f
        val z1 = if (prevScaleX > 0) scaleX / prevScaleX else scaleX
        bar_chart.zoom(z1, 1f, xPos, yPos)
        prevScaleX = scaleX

        //barChart.zoom(scaleX, scaleY, xPos, yPos, YAxis.AxisDependency.RIGHT);
        //barChart.moveViewToX(candleChart.getLowestVisibleX());
        //System.out.println("p.x" +p.x + " X: "+ scaleX + "z1: " + z1 + " C: " + candleChart.getScaleX() + " B: " + barChart.getScaleX());
    }


    override fun onChartTranslate(me: MotionEvent, dX: Float, dY: Float) {
//        checkInterval()
        bar_chart.moveViewToX(candle_chart.getLowestVisibleX())
    }
}
