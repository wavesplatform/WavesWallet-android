package com.wavesplatform.wallet.v2.ui.home.dex.trade.last_trades

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.gson.internal.bind.util.ISO8601Utils
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.model.remote.response.LastTradesResponse
import com.wavesplatform.wallet.v2.data.model.remote.response.MarketResponse
import com.wavesplatform.wallet.v2.util.clearBalance
import pyxis.uzuki.live.richutilskt.utils.asDateString
import java.math.BigDecimal
import java.text.ParsePosition
import javax.inject.Inject

class TradeLastTradesAdapter @Inject constructor() : BaseQuickAdapter<LastTradesResponse.Data.ExchangeTransaction, BaseViewHolder>(R.layout.recycle_item_last_trades, null) {
    var market: MarketResponse = MarketResponse()

    override fun convert(helper: BaseViewHolder, item: LastTradesResponse.Data.ExchangeTransaction) {
        val sum = item.price * item.amount

        helper
                .setText(R.id.text_time_value, ISO8601Utils.parse(item.timestamp, ParsePosition(0)).asDateString("HH:mm:ss"))
                .setText(R.id.text_price_value, item.price?.toBigDecimal().toPlainString())
                .setText(R.id.text_amount_value, item.amount.toBigDecimal().toPlainString())
                .setText(R.id.text_sum_value, BigDecimal(MoneyUtil.getFormattedTotal(sum, market.priceAssetDecimals).clearBalance()).toPlainString())
                .setTextColor(R.id.text_price_value, item.getMyOrder().getType().color)
    }
}