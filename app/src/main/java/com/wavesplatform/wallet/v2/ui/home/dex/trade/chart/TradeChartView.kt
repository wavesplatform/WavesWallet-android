/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex.trade.chart

import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CandleEntry
import com.wavesplatform.wallet.v2.data.model.remote.response.LastTradesResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface TradeChartView : BaseMvpView {
    fun successGetTrades(tradesMarket: LastTradesResponse.Data.ExchangeTransaction?)
    fun onShowCandlesSuccess(entries: ArrayList<CandleEntry>, barEntries: ArrayList<BarEntry>, firstRequest: Boolean)
    fun onRefreshCandles(ces: ArrayList<CandleEntry>, bes: ArrayList<BarEntry>)
    fun afterFailedLoadCandles(firstRequest: Boolean)
}
