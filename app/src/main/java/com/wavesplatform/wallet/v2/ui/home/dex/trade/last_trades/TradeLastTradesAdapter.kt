package com.wavesplatform.wallet.v2.ui.home.dex.trade.last_trades

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.model.remote.response.LastTrade
import com.wavesplatform.wallet.v2.data.model.remote.response.MarketResponse
import pers.victor.ext.findColor
import pyxis.uzuki.live.richutilskt.utils.asDateString
import javax.inject.Inject

class TradeLastTradesAdapter @Inject constructor() : BaseQuickAdapter<LastTrade, BaseViewHolder>(R.layout.recycle_item_last_trades, null) {
    var market: MarketResponse = MarketResponse()

    override fun convert(helper: BaseViewHolder, item: LastTrade) {
        val sum = item.price.toDouble() * item.amount.toDouble()
        helper.setText(R.id.text_time_value, item.timestamp.asDateString("HH:mm:ss"))
                .setText(R.id.text_price_value, item.price)
                .setText(R.id.text_amount_value, item.amount)
                .setText(R.id.text_sum_value, MoneyUtil.getFormattedTotal(sum, market.priceAssetDecimals))
                .setTextColor(R.id.text_price_value, item.getType().color)
    }
}