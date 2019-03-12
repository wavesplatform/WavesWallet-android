package com.wavesplatform.wallet.v2.ui.home.dex.trade.chart

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.MotionEvent
import android.widget.Button
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.jobs.MoveViewJob
import com.github.mikephil.charting.jobs.ZoomJob
import com.github.mikephil.charting.listener.BarLineChartTouchListener
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.utils.EntryXComparator
import com.github.mikephil.charting.utils.ObjectPool
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.model.local.ChartTimeFrame
import com.wavesplatform.wallet.v2.data.model.local.OrderType
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.data.model.remote.response.LastTradesResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.custom.CandleTouchListener
import com.wavesplatform.wallet.v2.ui.custom.OnCandleGestureListener
import com.wavesplatform.wallet.v2.ui.home.dex.trade.TradeActivity
import com.wavesplatform.wallet.v2.util.makeStyled
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.activity_trade.*
import kotlinx.android.synthetic.main.fragment_trade_chart.*
import kotlinx.android.synthetic.main.global_server_error_layout.*
import kotlinx.android.synthetic.main.layout_empty_data.*
import pers.victor.ext.click
import pers.victor.ext.findColor
import pers.victor.ext.gone
import pers.victor.ext.visiable
import pyxis.uzuki.live.richutilskt.utils.runDelayed
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import java.util.*
import javax.inject.Inject

class TradeChartFragment : BaseFragment(), TradeChartView, OnCandleGestureListener {

    @Inject
    @InjectPresenter
    lateinit var presenter: TradeChartPresenter
    var buttonPositive: Button? = null
    private var isLoading = false

    @ProvidePresenter
    fun providePresenter(): TradeChartPresenter = presenter

    override fun configLayoutRes() = com.wavesplatform.wallet.R.layout.fragment_trade_chart

    override fun onViewReady(savedInstanceState: Bundle?) {
        presenter.watchMarket = arguments?.getParcelable<WatchMarket>(TradeActivity.BUNDLE_MARKET)

        val timeFrame = ChartTimeFrame.findByServerTime(presenter.watchMarket?.market?.currentTimeFrame)
        val position = ChartTimeFrame.findPositionByServerTime(presenter.watchMarket?.market?.currentTimeFrame)

        timeFrame.notNull {
            presenter.selectedTimeFrame = position
            presenter.newSelectedTimeFrame = position

            text_change_time.text = getString(it.timeUI)
            presenter.currentTimeFrame = it.timeServer
        }

        text_empty.text = getString(com.wavesplatform.wallet.R.string.chart_empty)

        text_change_time.click {
            showTimeFrameDialog()
        }

        image_refresh.click {
            linear_charts.gone()
            progress_bar.show()

            presenter.loadCandles(Date().time, true)
            presenter.getTradesByPair()
        }

        button_retry.click {
            error_layout.gone()
            progress_bar.show()

            presenter.loadCandles(Date().time, true)
            presenter.getTradesByPair()
        }

        setUpChart()
        presenter.chartModel.pairModel = presenter.watchMarket
        presenter.startLoad()
    }

    private fun showTimeFrameDialog() {
        val alt_bld = AlertDialog.Builder(baseActivity)
        alt_bld.setTitle(getString(com.wavesplatform.wallet.R.string.chart_change_interval_dialog_title))
        alt_bld.setSingleChoiceItems(presenter.timeFrameList.map { getString(it.timeUI) }.toTypedArray(),
                presenter.selectedTimeFrame) { dialog, item ->
            if (presenter.selectedTimeFrame == item) {
                buttonPositive?.setTextColor(findColor(com.wavesplatform.wallet.R.color.basic300))
                buttonPositive?.isClickable = false
            } else {
                buttonPositive?.setTextColor(findColor(com.wavesplatform.wallet.R.color.submit400))
                buttonPositive?.isClickable = true
            }
            presenter.newSelectedTimeFrame = item
        }
        alt_bld.setPositiveButton(getString(com.wavesplatform.wallet.R.string.chart_change_interval_dialog_ok)) { dialog, which ->
            dialog.dismiss()
            presenter.selectedTimeFrame = presenter.newSelectedTimeFrame

            text_change_time.text = getString(presenter.timeFrameList[presenter.selectedTimeFrame].timeUI)
            presenter.currentTimeFrame = presenter.timeFrameList[presenter.selectedTimeFrame].timeServer

            linear_charts.gone()
            progress_bar.show()

            presenter.loadCandles(Date().time, true)
            presenter.getTradesByPair()

            rxEventBus.post(Events.UpdateMarketAfterChangeChartTimeFrame(presenter.watchMarket?.market?.id, presenter.timeFrameList[presenter.selectedTimeFrame].timeServer))
        }
        alt_bld.setNegativeButton(getString(com.wavesplatform.wallet.R.string.chart_change_interval_dialog_cancel)) { dialog, which -> dialog.dismiss() }
        val alert = alt_bld.create()
        alert.show()
        alert.makeStyled()

        buttonPositive = alert?.findViewById<Button>(android.R.id.button1)
        buttonPositive?.setTextColor(findColor(com.wavesplatform.wallet.R.color.basic300))
        buttonPositive?.isClickable = false
    }

    private fun setUpChart() {
        candle_chart.onTouchListener = CandleTouchListener(candle_chart, candle_chart.viewPortHandler.matrixTouch, 3f)

        val rightAxis = candle_chart.axisRight
        rightAxis.setDrawGridLines(true)
        rightAxis.textColor = Color.parseColor("#808080")
        rightAxis.textSize = 8f
        rightAxis.setDrawAxisLine(false)
        rightAxis.setLabelCount(10, true)
        rightAxis.maxWidth = 50f
        rightAxis.minWidth = 50f

        val leftAxis = candle_chart.axisLeft
        leftAxis.isEnabled = false

        val xAxis = candle_chart.xAxis
        xAxis.textSize = 11f
        xAxis.isGranularityEnabled = true
        xAxis.labelCount = 3

        xAxis.textColor = Color.parseColor("#808080")
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.isGranularityEnabled = true
        xAxis.valueFormatter = presenter.valueFormatter

        candle_chart.setView((activity as TradeActivity).viewpageer_trade)
        candle_chart.isScaleXEnabled = true
        candle_chart.isScaleYEnabled = false
        candle_chart.isAutoScaleMinMaxEnabled = true
        candle_chart.setVisibleXRange(10.0f, 100.0f)
        candle_chart.isDoubleTapToZoomEnabled = false
        candle_chart.setPinchZoom(false)

        presenter.chartModel.data.setData(CandleData())
        candle_chart.xAxis.valueFormatter = presenter.valueFormatter
        candle_chart.onChartGestureListener = this
        candle_chart.description.isEnabled = false
        candle_chart.setDrawGridBackground(false)
        candle_chart.legend.isEnabled = false
        candle_chart.extraBottomOffset = 25f
        candle_chart.extraLeftOffset = 15f

        bar_chart.setView((activity as TradeActivity).viewpageer_trade)
        bar_chart.onChartGestureListener = BarChartListener()
        bar_chart.setPinchZoom(false)
        bar_chart.isScaleXEnabled = false
        bar_chart.isScaleYEnabled = false
        bar_chart.setScaleEnabled(false)
        bar_chart.isDragEnabled = false
        bar_chart.setVisibleXRange(10.0f, 100.0f)
        bar_chart.isAutoScaleMinMaxEnabled = true
        bar_chart.description.isEnabled = false
        bar_chart.legend.isEnabled = false
        bar_chart.extraLeftOffset = 15f
        bar_chart.isDoubleTapToZoomEnabled = false

        bar_chart.axisLeft.isEnabled = false

        bar_chart.axisRight.maxWidth = 50f
        bar_chart.axisRight.minWidth = 50f
        bar_chart.axisRight.labelCount = 4
        bar_chart.axisRight.setDrawGridLines(true)
        bar_chart.axisRight.textColor = Color.parseColor("#808080")
        bar_chart.axisRight.textSize = 8f
        bar_chart.axisRight.setDrawAxisLine(false)
        bar_chart.axisRight.axisMinimum = 0f
        bar_chart.axisRight.labelCount = 4
        bar_chart.xAxis.setDrawLabels(false)
        bar_chart.xAxis.setDrawAxisLine(false)
        bar_chart.xAxis.isGranularityEnabled = true
        bar_chart.xAxis.labelCount = 3
    }

    private fun checkInterval() {
        val dataSetByIndex = candle_chart.candleData.getDataSetByIndex(0)
        val start = Math.round(dataSetByIndex.getEntryForIndex(0).x).toFloat()
        val lastOffsetX = Math.round(candle_chart.lowestVisibleX).toFloat()
        if (start == lastOffsetX && !isLoading) {
            isLoading = true

            candle_chart.cancelPendingInputEvents()
            (candle_chart.onTouchListener as BarLineChartTouchListener).stopDeceleration()
            candle_chart.disableScroll()
            candle_chart.isScaleXEnabled = false
            candle_chart.isScaleYEnabled = false
            candle_chart.isDragEnabled = false

            bar_chart.cancelPendingInputEvents()
            (bar_chart.onTouchListener as BarLineChartTouchListener).stopDeceleration()
            bar_chart.disableScroll()
            bar_chart.isScaleXEnabled = false
            bar_chart.isScaleYEnabled = false
            bar_chart.isDragEnabled = false

            val lastDate = Date(lastOffsetX.toLong() * 1000 * 60 * presenter.currentTimeFrame.toLong())
            presenter.loadCandles(lastDate.time, false)
            presenter.getTradesByPair()
        }
    }

    override fun onPause() {
        presenter.pause()
        super.onPause()
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
        }
    }

    override fun onChartGestureStart(me: MotionEvent, lastPerformedGesture: ChartTouchListener.ChartGesture) {}

    override fun onChartGestureEnd(me: MotionEvent, lastPerformedGesture: ChartTouchListener.ChartGesture) {
        prevScaleX = 0f
        checkInterval()
    }

    override fun onChartUp(me: MotionEvent) {
        if (!isLoading && !candle_chart.isDragEnabled) {
            candle_chart.isScaleXEnabled = true
            candle_chart.isDragEnabled = true
        }
    }

    override fun onChartLongPressed(me: MotionEvent) {
    }

    override fun onChartDoubleTapped(me: MotionEvent) {
        // checkInterval();
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
    }

    override fun onChartTranslate(me: MotionEvent, dX: Float, dY: Float) {
        bar_chart.moveViewToX(candle_chart.lowestVisibleX)
    }

    override fun successGetTrades(tradesMarket: LastTradesResponse.Data.ExchangeTransaction?) {
        tradesMarket.notNull {
            val limitLine = LimitLine(it.price.toFloat(), "")
            limitLine.lineColor = if (it.getMyOrder().getType() == OrderType.BUY) findColor(com.wavesplatform.wallet.R.color.submit300) else findColor(com.wavesplatform.wallet.R.color.error400)
            limitLine.lineWidth = 1f
            limitLine.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
            candle_chart.axisRight.removeAllLimitLines()
            candle_chart.axisRight.addLimitLine(limitLine)
            candle_chart.axisRight.setDrawLimitLinesBehindData(false)
            candle_chart.invalidate()
        }
    }

    override fun onShowCandlesSuccess(candles: ArrayList<CandleEntry>, barEntries: ArrayList<BarEntry>, firstRequest: Boolean) {
        if (firstRequest) {
            progress_bar.hide()
            if (candles.isEmpty() && barEntries.isEmpty()) {
                relative_timeframe.gone()
                linear_charts.gone()
                layout_empty.visiable()
            } else {
                relative_timeframe.visiable()
                linear_charts.visiable()
                val barData = BarData()
                val set1 = BarDataSet(barEntries, "Bar 1")
                set1.setDrawValues(false)
                set1.isHighlightEnabled = false
                set1.color = Color.parseColor("#cccccc")
                set1.axisDependency = YAxis.AxisDependency.RIGHT
                barData.addDataSet(set1)

                val candleData = CandleData()
                val set = CandleDataSet(candles, "Candle DataSet")
                set.decreasingColor = findColor(com.wavesplatform.wallet.R.color.error400)
                set.increasingColor = findColor(com.wavesplatform.wallet.R.color.submit300)
                set.neutralColor = Color.parseColor("#4b7190")
                set.shadowColorSameAsCandle = true
                set.increasingPaintStyle = Paint.Style.FILL
                set.shadowColor = Color.DKGRAY
                set.isHighlightEnabled = false
                set.setDrawValues(false)
                set.axisDependency = YAxis.AxisDependency.RIGHT
                candleData.addDataSet(set)

                val last = candles[candles.size - 1]
                candle_chart.data = candleData

                bar_chart.data = barData

                bar_chart.setVisibleXRangeMinimum(5f)
                bar_chart.setVisibleXRangeMaximum(100f)

                candle_chart.setVisibleXRangeMinimum(5f)
                candle_chart.setVisibleXRangeMaximum(100f)

                candle_chart.moveViewToX(set.getEntryForIndex(set.entryCount - 1).x)
                bar_chart.moveViewToX(set.getEntryForIndex(set.entryCount - 1).x)

                bar_chart.zoom(4f / bar_chart.scaleX, 1f, last.x, last.y, YAxis.AxisDependency.RIGHT)
                candle_chart.zoom(4f / candle_chart.scaleX, 1f, last.x, last.y, YAxis.AxisDependency.RIGHT)

                bar_chart.invalidate()
                candle_chart.invalidate()
            }
        } else {
            runDelayed(100) {
                runOnUiThread {
                    updateCandles(candles, barEntries)
                }
            }
        }
    }

    private fun updateCandles(candles: List<CandleEntry>, barEntries: List<BarEntry>) {
        if (bar_chart != null && candle_chart != null) {
            val barData = BarData()
            val baseDataSet = bar_chart.barData.getDataSetByIndex(0) as BarDataSet

            barEntries.iterator().forEach {
                baseDataSet.addEntry(it)
            }

            Collections.sort(baseDataSet.values, EntryXComparator())
            barData.addDataSet(baseDataSet)

            val candleDataSet = candle_chart.candleData.getDataSetByIndex(0) as CandleDataSet
            val prevCnt = candleDataSet.entryCount
            val lastPoint = candleDataSet.getEntryForIndex(0)

            candles.iterator().forEach {
                candleDataSet.addEntry(it)
            }

            Collections.sort(candleDataSet.values, EntryXComparator())
            val candleData = CandleData()
            candleData.addDataSet(candleDataSet)

            candle_chart.data = candleData
            bar_chart.data = barData

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
    }

    override fun onRefreshCandles(candles: ArrayList<CandleEntry>, barEntries: ArrayList<BarEntry>) {
        if (candles.isEmpty() || barEntries.isEmpty())
            return

        val baseDataSet = bar_chart.barData.getDataSetByIndex(0) as BarDataSet

        barEntries.iterator().forEach { barEntry ->
            val entries = baseDataSet.getEntriesForXValue(barEntry.x)
            if (entries.isEmpty()) {
                baseDataSet.addEntry(barEntry)
            } else {
                val bar = entries[0]
                bar.y = barEntry.y
            }
        }

        baseDataSet.notifyDataSetChanged()
        val barData = BarData()
        barData.addDataSet(baseDataSet)
        bar_chart.data = barData
        bar_chart.notifyDataSetChanged()

        val candleDataSet = candle_chart.candleData.getDataSetByIndex(0) as CandleDataSet
        candles.iterator().forEach { candleEntry ->
            val entries = candleDataSet.getEntriesForXValue(candleEntry.x)
            if (entries.isEmpty()) {
                candleDataSet.addEntry(candleEntry)
            } else {
                val c = entries[0]
                c.high = candleEntry.high
                c.open = candleEntry.open
                c.low = candleEntry.low
                c.close = candleEntry.close
            }
        }

        val candleData = CandleData()
        candleData.addDataSet(candleDataSet)
        candle_chart.data = candleData
        candle_chart.notifyDataSetChanged()
    }

    override fun afterFailedLoadCandles(firstRequest: Boolean) {
        progress_bar.hide()
        if (firstRequest) {
            relative_timeframe.gone()
            linear_charts.gone()

            error_layout.visiable()

            presenter.pause()
        }
    }

    override fun onDestroyView() {
        fixChartMemoryLeaks()

        bar_chart.onDestroy()
        candle_chart.onDestroy()
        super.onDestroyView()
    }

    private fun fixChartMemoryLeaks() {
        // Fix https://github.com/PhilJay/MPAndroidChart/issues/2238
        val moveViewJobPoll = MoveViewJob::class.java.getDeclaredField("pool")
        moveViewJobPoll.isAccessible = true
        moveViewJobPoll.set(null, ObjectPool.create(2, MoveViewJob(null, 0f, 0f, null, null)))

        // the same issue with ZoomJob
        val zoomViewJobPoll = ZoomJob::class.java.getDeclaredField("pool")
        zoomViewJobPoll.isAccessible = true
        zoomViewJobPoll.set(null, ObjectPool.create(2, ZoomJob(null, 0f, 0f, 0f, 0f, null, null, null)))
    }

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
}
