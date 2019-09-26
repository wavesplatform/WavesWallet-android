/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex.trade.chart

import com.arellomobile.mvp.InjectViewState
import com.crashlytics.android.Crashlytics
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.vicpin.krealmextensions.save
import com.wavesplatform.wallet.v2.data.model.remote.response.WatchMarketResponse
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.wallet.v2.data.model.db.userdb.MarketResponseDb
import com.wavesplatform.wallet.v2.data.model.local.ChartModel
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.EnvironmentManager
import com.wavesplatform.wallet.v2.util.WavesWallet
import io.reactivex.Observable
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@InjectViewState
class TradeChartPresenter @Inject constructor() : BasePresenter<TradeChartView>() {
    var watchMarket: WatchMarketResponse? = null
    var selectedTimeFrame = 0
    var newSelectedTimeFrame = 0

    var chartModel: ChartModel = ChartModel()
    private var entries: ArrayList<CandleEntry> = ArrayList()
    private var barEntries: ArrayList<BarEntry> = ArrayList()
    var currentTimeFrame: Int = 30
        set(value) {
            if (!WavesWallet.isAuthenticated()) {
                return
            }
            field = value
            watchMarket?.market?.currentTimeFrame = value
            watchMarket?.market.notNull {
                MarketResponseDb(it).save()
            }
        }
    private var prevToDate: Long = 0
    private var timer: Timer? = null

    internal var valueFormatter = IAxisValueFormatter { value, axis ->
        val simpleDateFormat = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
        simpleDateFormat.timeZone = TimeZone.getDefault()

        val date = Date(value.toLong() * 1000 * 60 * currentTimeFrame.toLong())
        simpleDateFormat.format(date)
    }

    fun startLoad() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = EnvironmentManager.getTime()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 2)
        chartModel.lastLoadDate = calendar.time

        currentTimeFrame = if (chartModel.pairModel?.market?.currentTimeFrame != null)
            chartModel.pairModel?.market?.currentTimeFrame!!
        else 30

        loadCandles(EnvironmentManager.getTime(), true)
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

        addSubscription(dataServiceManager.loadCandles(watchMarket, currentTimeFrame, fromTimestamp, to)
                .flatMap { candles ->
                    chartModel.candleList = candles
                    Observable.fromIterable(candles)
                }
                .filter { candle -> candle.volume != null }
                .filter { candle -> candle.volume!! > 0 }
                .map { candle ->
                    val e = CandleEntry((candle.getTimeInMillis() / (1000 * 60 * currentTimeFrame)).toFloat(), candle.high!!.toFloat(), candle.low!!.toFloat(), candle.openValue!!.toFloat(), candle.close!!.toFloat())
                    entries.add(e)

                    barEntries.add(BarEntry((candle.getTimeInMillis() / (1000 * 60 * currentTimeFrame)).toFloat(), candle.volume!!.toFloat()))

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
                    Crashlytics.logException(Exception("TradeChartPresenter loadCandles() error", it))
                    viewState.afterFailedLoadCandles(firstRequest)
                }))
    }

    fun refreshCandles() {
        val to = EnvironmentManager.getTime()
        addSubscription(dataServiceManager.loadCandles(
                watchMarket,
                currentTimeFrame, prevToDate, to)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ candles ->
                    val ces = ArrayList<CandleEntry>()
                    val bes = ArrayList<BarEntry>()
                    for (candle in candles) {
                        if (candle.volume != null) {
                            if (candle.volume!! > 0) {
                                ces.add(CandleEntry((candle.getTimeInMillis()
                                        / (1000 * 60 * currentTimeFrame)).toFloat(),
                                        candle.high!!.toFloat(),
                                        candle.low!!.toFloat(),
                                        candle.openValue!!.toFloat(),
                                        candle.close!!.toFloat()))
                                bes.add(BarEntry((candle.getTimeInMillis()
                                        / (1000 * 60 * currentTimeFrame)).toFloat(),
                                        candle.volume!!.toFloat()))
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
        addSubscription(dataServiceManager.getLastExchangesByPair(watchMarket?.market?.amountAsset,
                watchMarket?.market?.priceAsset,
                DEFAULT_LIMIT)
                .map { it.firstOrNull() }
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ tradesMarket ->
                    viewState.successGetTrades(tradesMarket)
                }, { it.printStackTrace() }))
    }

    companion object {
        var DEFAULT_LIMIT = 1
    }
}
