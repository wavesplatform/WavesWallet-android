package com.wavesplatform.wallet.v2.ui.home.dex.trade.orderbook

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface TradeOrderBookView : BaseMvpView {
    fun afterSuccessOrderbook(data: MutableList<MultiItemEntity>, lastPricePosition: Int)
    fun afterFailedOrderbook()
}
