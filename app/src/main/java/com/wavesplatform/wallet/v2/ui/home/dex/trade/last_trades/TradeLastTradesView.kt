package com.wavesplatform.wallet.v2.ui.home.dex.trade.last_trades

import com.wavesplatform.wallet.v2.data.model.local.MyOrderItem
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView
import com.wavesplatform.wallet.v2.ui.home.history.TestObject

interface TradeLastTradesView : BaseMvpView {
    fun afterSuccessLoadLastTrades(data: ArrayList<TestObject>)

}
