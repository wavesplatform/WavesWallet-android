package com.wavesplatform.wallet.v2.ui.home.dex.trade.orderbook

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.model.local.LastPriceItem
import com.wavesplatform.wallet.v2.data.model.local.OrderType
import com.wavesplatform.wallet.v2.data.model.remote.response.LastTrade
import com.wavesplatform.wallet.v2.data.model.remote.response.MarketResponse
import com.wavesplatform.wallet.v2.data.model.remote.response.OrderBook
import com.wavesplatform.wallet.v2.util.stripZeros
import kotlinx.android.synthetic.main.recycle_item_orderbook.view.*
import pers.victor.ext.findColor
import pers.victor.ext.setWidth
import javax.inject.Inject
import android.widget.RelativeLayout
import com.wavesplatform.wallet.v2.util.clearBalance


class TradeOrderBookAdapter @Inject constructor() : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(null) {
    var market: MarketResponse = MarketResponse()

    companion object {
        val LAST_PRICE_TYPE = 1
        val BID_TYPE = 2
        val ASK_TYPE = 3
    }

    init {
        addItemType(LAST_PRICE_TYPE, R.layout.recycle_item_orderbook_last_price)
        addItemType(BID_TYPE, R.layout.recycle_item_orderbook)
        addItemType(ASK_TYPE, R.layout.recycle_item_orderbook)
    }

    override fun convert(helper: BaseViewHolder, item: MultiItemEntity) {
        when (helper.itemViewType) {
            LAST_PRICE_TYPE -> {
                val item = item as LastPriceItem
                val percent = "%.2f".format(item.spreadPercent)
                helper.setImageResource(R.id.image_graph,
                        if (item.lastTrade?.getType() == OrderType.BUY) R.drawable.ic_chartarrow_success_400
                        else R.drawable.ic_chartarrow_error_500)
                        .setText(R.id.text_price_value, item.lastTrade?.price?.stripZeros())
                        .setText(R.id.text_percent_value, mContext.getString(R.string.orderbook_spread_percent, percent))
            }
            ASK_TYPE -> {
                val item = item as OrderBook.Ask
                val amountUIValue = MoneyUtil.getScaledText(item.amount, market.amountAssetDecimals).stripZeros()
                val priceUIValue = MoneyUtil.getScaledPrice(item.price, market.amountAssetDecimals, market.priceAssetDecimals).stripZeros()
                helper.setTextColor(R.id.text_price_value, findColor(R.color.error400))
                        .setBackgroundColor(R.id.view_bg, findColor(R.color.error100))
                        .setText(R.id.text_amount_value, amountUIValue)
                        .setText(R.id.text_price_value, priceUIValue)
                        .setText(R.id.text_sum_value, MoneyUtil.getFormattedTotal(item.sum, market.priceAssetDecimals))

            }
            BID_TYPE -> {
                val item = item as OrderBook.Bid
                val amountUIValue = MoneyUtil.getScaledText(item.amount, market.amountAssetDecimals).stripZeros()
                val priceUIValue = MoneyUtil.getScaledPrice(item.price, market.amountAssetDecimals, market.priceAssetDecimals).stripZeros()
                helper.setTextColor(R.id.text_price_value, findColor(R.color.submit400))
                        .setBackgroundColor(R.id.view_bg, findColor(R.color.submit50))
                        .setText(R.id.text_amount_value, amountUIValue)
                        .setText(R.id.text_price_value, priceUIValue)
                        .setText(R.id.text_sum_value, MoneyUtil.getFormattedTotal(item.sum, market.priceAssetDecimals))
            }
        }

    }
}