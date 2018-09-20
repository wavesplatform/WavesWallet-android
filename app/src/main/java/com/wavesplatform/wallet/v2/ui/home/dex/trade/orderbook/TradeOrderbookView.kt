package com.wavesplatform.wallet.v2.ui.home.dex.trade.orderbook

import com.wavesplatform.wallet.v2.data.model.local.OrderbookItem
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView
import java.util.*

interface TradeOrderbookView : BaseMvpView {
    fun afterSuccessOrderbook(data: ArrayList<OrderbookItem>)
}
