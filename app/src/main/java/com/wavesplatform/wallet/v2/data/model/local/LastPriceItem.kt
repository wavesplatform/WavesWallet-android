/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.sdk.model.response.data.LastTradesResponse
import com.wavesplatform.wallet.v2.ui.home.dex.trade.orderbook.TradeOrderBookAdapter

class LastPriceItem : MultiItemEntity {
    var lastTrade: LastTradesResponse.DataResponse.ExchangeTransactionResponse? = null
    var spreadPercent: Double? = 0.0

    constructor(spreadPercent: Double?, item: LastTradesResponse.DataResponse.ExchangeTransactionResponse) {
        this.lastTrade = item
        this.spreadPercent = spreadPercent
    }

    override fun getItemType(): Int {
        return TradeOrderBookAdapter.LAST_PRICE_TYPE
    }
}