/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex.trade.last_trades

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.gson.internal.bind.util.ISO8601Utils
import com.wavesplatform.wallet.R
import com.wavesplatform.sdk.utils.MoneyUtil
import com.wavesplatform.sdk.model.response.data.LastTradesResponse
import com.wavesplatform.sdk.model.response.matcher.MarketResponse
import com.wavesplatform.sdk.utils.clearBalance
import com.wavesplatform.wallet.v2.util.getType
import pyxis.uzuki.live.richutilskt.utils.asDateString
import java.math.BigDecimal
import java.text.ParsePosition
import javax.inject.Inject

class TradeLastTradesAdapter @Inject constructor() : BaseQuickAdapter<LastTradesResponse.DataResponse.ExchangeTransactionResponse, BaseViewHolder>(R.layout.item_last_trades, null) {
    var market: MarketResponse = MarketResponse()

    override fun convert(helper: BaseViewHolder, item: LastTradesResponse.DataResponse.ExchangeTransactionResponse) {
        val sum = item.price * item.amount

        helper
                .setText(R.id.text_time_value, ISO8601Utils.parse(item.timestamp, ParsePosition(0)).asDateString("HH:mm:ss"))
                .setText(R.id.text_price_value, item.price?.toBigDecimal().toPlainString())
                .setText(R.id.text_amount_value, item.amount.toBigDecimal().toPlainString())
                .setText(R.id.text_sum_value, BigDecimal(MoneyUtil.getFormattedTotal(sum, market.priceAssetDecimals).clearBalance()).toPlainString())
                .setTextColor(R.id.text_price_value, item.getMyOrder().getType().color)
    }
}