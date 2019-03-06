package com.wavesplatform.wallet.v2.ui.home.dex.trade.chart

import com.arellomobile.mvp.InjectViewState
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.vicpin.krealmextensions.save
import com.wavesplatform.wallet.v2.data.model.local.ChartModel
import com.wavesplatform.wallet.v2.data.model.local.ChartTimeFrame
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import io.reactivex.Observable
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@InjectViewState
class TradeChartPresenter @Inject constructor() : BasePresenter<TradeChartView>() {
    var watchMarket: WatchMarket? = null
    var selectedTimeFrame = 0
    var newSelectedTimeFrame = 0
    val timeFrameList = arrayOf(ChartTimeFrame.FIVE_MINUTES, ChartTimeFrame.FIFTEEN_MINUTES, ChartTimeFrame.THIRTY_MINUTES,
            ChartTimeFrame.ONE_HOUR, ChartTimeFrame.FOUR_HOURS, ChartTimeFrame.TWENTY_FOUR_HOURS)

    var chartModel: ChartModel = ChartModel()
    private var entries: ArrayList<CandleEntry> = ArrayList()
    private var barEntries: ArrayList<BarEntry> = ArrayList()
    var currentTimeFrame: Int = 30
        set(value) {
            field = value
            watchMarket?.market?.currentTimeFrame = value
            watchMarket?.market?.save()
        }
    var prevToDate: Long = 0
    private var timer: Timer? = null

    internal var valueFormatter = IAxisValueFormatter { value, axis ->
        val simpleDateFormat = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())

        val date = Date(value.toLong() * 1000 * 60 * currentTimeFrame.toLong())
        simpleDateFormat.format(date)
    }

    fun startLoad() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 2)
        chartModel.lastLoadDate = calendar.time

        currentTimeFrame = if (chartModel.pairModel?.market?.currentTimeFrame != null)
            chartModel.pairModel?.market?.currentTimeFrame!!
        else
            30

        loadCandles(Date().time, true)
        getTradesByPair()

        startTimer()
    }

    fun pause() {
        timer?.cancel()
        timer = null
    }

    fun resume() {
        startTimer()
    }

    private fun startTimer() {
        if (timer != null) pause()

        if (timer == null) {
            timer = Timer()
            val timerTask = object : TimerTask() {
                override fun run() {
                    refreshCandles()
                    getTradesByPair()
                }
            }
            timer?.scheduleAtFixedRate(timerTask, (1000 * 60).toLong(), (1000 * 60).toLong())
        }
    }

    fun loadCandles(to: Long?, firstRequest: Boolean) {
        entries = ArrayList()
        barEntries = ArrayList()
        val fromTimestamp = to!! - 100L * currentTimeFrame.toLong() * 1000 * 60

        addSubscription(apiDataManager.loadCandles(watchMarket, currentTimeFrame, fromTimestamp, to)
                .flatMap { candles ->
                    chartModel.candleList = candles
                    Observable.fromIterable(candles)
                }
                .filter { candle -> candle.volume != null }
                .filter { candle -> candle.volume!! > 0 }
                .map { candle ->
                    val e = CandleEntry((candle.time!! / (1000 * 60 * currentTimeFrame)).toFloat(), candle.high!!.toFloat(), candle.low!!.toFloat(), candle.openValue!!.toFloat(), candle.close!!.toFloat())
                    entries.add(e)

                    barEntries.add(BarEntry((candle.time!! / (1000 * 60 * currentTimeFrame)).toFloat(), candle.volume!!.toFloat()))

                    candle
                }
                .toList().toObservable()
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ tx ->
                    if (firstRequest)
                        prevToDate = to
                    viewState.onShowCandlesSuccess(entries, barEntries, firstRequest)
                }, {
                    it.printStackTrace()
                    viewState.afterFailedLoadCandles(firstRequest)
                }))
    }

    fun refreshCandles() {
        val to = Date().time
        addSubscription(apiDataManager.loadCandles(
                watchMarket,
                currentTimeFrame, prevToDate, to)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ candles ->
                    val ces = ArrayList<CandleEntry>()
                    val bes = ArrayList<BarEntry>()
                    for (candle in candles) {
                        if (candle.volume != null) {
                            if (candle.volume!! > 0) {
                                ces.add(CandleEntry((candle.time!! / (1000 * 60 * currentTimeFrame)).toFloat(), candle.high!!.toFloat(), candle.low!!.toFloat(), candle.openValue!!.toFloat(), candle.close!!.toFloat()))
                                bes.add(BarEntry((candle.time!! / (1000 * 60 * currentTimeFrame)).toFloat(), candle.volume!!.toFloat()))
                            }
                        }
                    }
                    prevToDate = to
                    viewState.onRefreshCandles(ces, bes)
                }, {
                    it.printStackTrace()
                }))
    }

    fun getTradesByPair() {
        addSubscription(apiDataManager.getLastTradeByPair(watchMarket)
                .map { it.firstOrNull() }
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ tradesMarket ->
                    viewState.successGetTrades(tradesMarket)
                }, { it.printStackTrace() }))
    }
}
