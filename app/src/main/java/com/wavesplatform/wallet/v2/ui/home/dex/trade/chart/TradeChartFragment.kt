package com.wavesplatform.wallet.v2.ui.home.dex.trade.chart

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.listener.BarLineChartTouchListener
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.utils.EntryXComparator
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.ui.dex.details.chart.CandleTouchListener
import com.wavesplatform.wallet.v1.ui.dex.details.chart.OnCandleGestureListener
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.data.model.remote.response.LastTrade
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.TradeActivity
import kotlinx.android.synthetic.main.activity_trade.*
import kotlinx.android.synthetic.main.fragment_trade_chart.*
import kotlinx.android.synthetic.main.layout_empty_data.*
import java.util.*
import javax.inject.Inject


class TradeChartFragment : BaseFragment(), TradeChartView, OnCandleGestureListener {
    override fun successGetTrades(tradesMarket: List<LastTrade>) {
        if (tradesMarket.size < 1) return

        val limitLine = LimitLine(java.lang.Float.valueOf(tradesMarket[0].price)!!, "")
        limitLine.lineColor = Color.parseColor("#F6AD12")
        limitLine.lineWidth = 1f
        limitLine.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
        candle_chart.getAxisRight().removeAllLimitLines()
        candle_chart.getAxisRight().addLimitLine(limitLine)
        candle_chart.getAxisRight().setDrawLimitLinesBehindData(false)
        candle_chart.invalidate()
    }

    override fun onShowCandlesSuccess(candles: ArrayList<CandleEntry>, barEntries: ArrayList<BarEntry>, firstRequest: Boolean) {

        if (firstRequest) {
            val barData = BarData()
            val set1 = BarDataSet(barEntries, "Bar 1")
            set1.setDrawValues(false)
            set1.isHighlightEnabled = false
            set1.color = Color.parseColor("#cccccc")//
            set1.axisDependency = YAxis.AxisDependency.RIGHT
            barData.addDataSet(set1)

            val candleData = CandleData()
            val set = CandleDataSet(candles, "Candle DataSet")
            set.decreasingColor = Color.parseColor("#E66C69")
            set.increasingColor = Color.parseColor("#5EAC69")
            set.neutralColor = Color.parseColor("#4b7190")
            set.shadowColorSameAsCandle = true
            set.increasingPaintStyle = Paint.Style.FILL
            set.shadowColor = Color.DKGRAY
            set.isHighlightEnabled = false
            set.setDrawValues(false)
            set.axisDependency = YAxis.AxisDependency.RIGHT
            candleData.addDataSet(set)

            val last = candles.get(candles.size - 1)
            candle_chart.setData(candleData)

            bar_chart.setData(barData)

            bar_chart.setVisibleXRangeMinimum(5f)
            bar_chart.setVisibleXRangeMaximum(100f)

            candle_chart.setVisibleXRangeMinimum(5f)
            candle_chart.setVisibleXRangeMaximum(100f)

            candle_chart.moveViewToX(set.getEntryForIndex(set.entryCount - 1).x)
            bar_chart.moveViewToX(set.getEntryForIndex(set.entryCount - 1).x)

            bar_chart.zoom(4f / bar_chart.getScaleX(), 1f, last.getX(), last.getY(), YAxis.AxisDependency.RIGHT)
            candle_chart.zoom(4f / candle_chart.getScaleX(), 1f, last.getX(), last.getY(), YAxis.AxisDependency.RIGHT)

            bar_chart.invalidate()
            candle_chart.invalidate()

        } else {
            Handler().postDelayed({ updateCandles(candles, barEntries) }, 100)
        }

    }

    private fun updateCandles(candles: List<CandleEntry>, barEntries: List<BarEntry>) {
        val barData = BarData()
        val baseDataSet = bar_chart.getBarData().getDataSetByIndex(0) as BarDataSet

        for (barEntry in barEntries) {
            baseDataSet.addEntry(barEntry)
        }
        Collections.sort(baseDataSet.values, EntryXComparator())
        barData.addDataSet(baseDataSet)

        val candleDataSet = candle_chart.getCandleData().getDataSetByIndex(0) as CandleDataSet
        val prevCnt = candleDataSet.entryCount
        val lastPoint = candleDataSet.getEntryForIndex(0)
        for (candleEntry in candles) {
            candleDataSet.addEntry(candleEntry)
        }
        Collections.sort(candleDataSet.values, EntryXComparator())
        val candleData = CandleData()
        candleData.addDataSet(candleDataSet)

        candle_chart.setData(candleData)
        bar_chart.setData(barData)

        candle_chart.moveViewToX(lastPoint.x)
        bar_chart.moveViewToX(lastPoint.x)

        val zoom = if (prevCnt > 0) candleDataSet.entryCount.toFloat() / prevCnt else 0.0f

        candle_chart.zoomToCenter(zoom, 0.0f)
        candle_chart.setVisibleXRangeMinimum(5f)
        candle_chart.setVisibleXRangeMaximum(100f)
        candle_chart.notifyDataSetChanged()

        bar_chart.zoomToCenter(zoom, 0.0f)
        bar_chart.setVisibleXRangeMinimum(5f)
        bar_chart.setVisibleXRangeMaximum(100f)
        bar_chart.notifyDataSetChanged()

        isLoading = false
    }


    override fun onRefreshCandles(candles: ArrayList<CandleEntry>, barEntries: ArrayList<BarEntry>) {
        if (candles.isEmpty() || barEntries.isEmpty())
            return

        val baseDataSet = bar_chart.getBarData().getDataSetByIndex(0) as BarDataSet

        for (barEntry in barEntries) {
            val entries = baseDataSet.getEntriesForXValue(barEntry.getX())
            if (entries.isEmpty()) {
                baseDataSet.addEntry(barEntry)
            } else {
                val bar = entries[0]
                bar.y = barEntry.getY()
            }
        }
        baseDataSet.notifyDataSetChanged()
        val barData = BarData()
        barData.addDataSet(baseDataSet)
        bar_chart.setData(barData)
        //barChart.moveViewToX(baseDataSet.getEntryForIndex(baseDataSet.getEntryCount() - 1).getX());
        bar_chart.notifyDataSetChanged()

        val candleDataSet = candle_chart.getCandleData().getDataSetByIndex(0) as CandleDataSet
        for (candleEntry in candles) {
            val entries = candleDataSet.getEntriesForXValue(candleEntry.getX())
            if (entries.isEmpty()) {
                candleDataSet.addEntry(candleEntry)
            } else {
                val c = entries[0]
                c.high = candleEntry.getHigh()
                c.open = candleEntry.getOpen()
                c.low = candleEntry.getLow()
                c.close = candleEntry.getClose()
            }
        }
        val candleData = CandleData()
        candleData.addDataSet(candleDataSet)
        candle_chart.setData(candleData)
        //candleChart.moveViewToX(candleDataSet.getEntryForIndex(candleDataSet.getEntryCount() - 1).getX());
        candle_chart.notifyDataSetChanged()
    }

    @Inject
    @InjectPresenter
    lateinit var presenter: TradeChartPresenter

    private var isLoading = false

    companion object {
        fun newInstance(watchMarket: WatchMarket?): TradeChartFragment {
            val args = Bundle()
            args.classLoader = WatchMarket::class.java.classLoader
            args.putParcelable(TradeActivity.BUNDLE_MARKET, watchMarket)
            val fragment = TradeChartFragment()
            fragment.arguments = args
            return fragment
        }
    }

    @ProvidePresenter
    fun providePresenter(): TradeChartPresenter = presenter

    override fun configLayoutRes() = R.layout.fragment_trade_chart


    override fun onViewReady(savedInstanceState: Bundle?) {
        presenter.watchMarket = arguments?.getParcelable<WatchMarket>(TradeActivity.BUNDLE_MARKET)

        text_empty.text = getString(R.string.chart_empty)

        setUpChart()
        presenter.chartModel.pairModel = presenter.watchMarket
        presenter.startLoad()
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
        xAxis.setValueFormatter(presenter.valueFormatter)

        candle_chart.setView((activity as TradeActivity).viewpageer_trade)
        candle_chart.setScaleXEnabled(true)
        candle_chart.setScaleYEnabled(false)
        candle_chart.setAutoScaleMinMaxEnabled(true)
        candle_chart.setVisibleXRange(10.0f, 100.0f)
        candle_chart.setDoubleTapToZoomEnabled(false)
        candle_chart.setPinchZoom(false)

        presenter.chartModel.data.setData(CandleData())
//
        candle_chart.getXAxis().setValueFormatter(presenter.valueFormatter)
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


    private fun checkInterval() {
        val dataSetByIndex = candle_chart.getCandleData().getDataSetByIndex(0)
        val start = Math.round(dataSetByIndex.getEntryForIndex(0).getX()).toFloat()
        val lastOffsetX = Math.round(candle_chart.getLowestVisibleX()).toFloat()
        if (start == lastOffsetX && !isLoading) {
            isLoading = true

            candle_chart.cancelPendingInputEvents()
            (candle_chart.getOnTouchListener() as BarLineChartTouchListener).stopDeceleration()
            candle_chart.disableScroll()
            candle_chart.setScaleXEnabled(false)
            candle_chart.setScaleYEnabled(false)
            candle_chart.setDragEnabled(false)

            bar_chart.cancelPendingInputEvents()
            (bar_chart.getOnTouchListener() as BarLineChartTouchListener).stopDeceleration()
            bar_chart.disableScroll()
            bar_chart.setScaleXEnabled(false)
            bar_chart.setScaleYEnabled(false)
            bar_chart.setDragEnabled(false)

            val lastDate = Date(lastOffsetX.toLong() * 1000 * 60 * presenter.currentTimeFrame.toLong())
            presenter.loadCandles(lastDate.getTime(), false)
            presenter.getTradesByPair()
        }
    }

    override fun onPause() {
        super.onPause()
        presenter.pause()
    }

    override fun onResume() {
        super.onResume()
        presenter.resume()
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
        checkInterval()
    }

    override fun onChartUp(me: MotionEvent) {
        if (!isLoading && !candle_chart.isDragEnabled()) {
            candle_chart.setScaleXEnabled(true)
            candle_chart.setDragEnabled(true)
        }
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
