package com.wavesplatform.wallet.v2.ui.home.dex.trade.last_trades

import com.wavesplatform.wallet.v2.data.model.remote.response.LastTrade
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface TradeLastTradesView : BaseMvpView {
    fun afterSuccessLoadLastTrades(data: List<LastTrade>)
    fun afterFailedLoadLastTrades()
}
