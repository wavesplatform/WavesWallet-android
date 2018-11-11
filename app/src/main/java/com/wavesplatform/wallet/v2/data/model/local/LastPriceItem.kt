package com.wavesplatform.wallet.v2.data.model.local

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.wallet.v2.data.model.remote.response.LastTrade
import com.wavesplatform.wallet.v2.ui.home.dex.trade.orderbook.TradeOrderBookAdapter

class LastPriceItem : MultiItemEntity {
    var lastTrade: LastTrade? = null
    var spreadPercent: Double? = 0.0


    constructor(spreadPercent: Double?, item: LastTrade) {
        this.lastTrade = item
        this.spreadPercent = spreadPercent
    }

    override fun getItemType(): Int {
        return TradeOrderBookAdapter.LAST_PRICE_TYPE
    }
}