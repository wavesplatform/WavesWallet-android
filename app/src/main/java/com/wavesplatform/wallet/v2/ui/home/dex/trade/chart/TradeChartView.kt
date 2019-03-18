package com.wavesplatform.wallet.v2.ui.home.dex.trade.chart

import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CandleEntry
import com.wavesplatform.sdk.net.model.response.LastTradesResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface TradeChartView : BaseMvpView {
    fun successGetTrades(tradesMarket: LastTradesResponse.Data.ExchangeTransaction?)
    fun onShowCandlesSuccess(entries: ArrayList<CandleEntry>, barEntries: ArrayList<BarEntry>, firstRequest: Boolean)
    fun onRefreshCandles(ces: ArrayList<CandleEntry>, bes: ArrayList<BarEntry>)
    fun afterFailedLoadCandles(firstRequest: Boolean)
}
