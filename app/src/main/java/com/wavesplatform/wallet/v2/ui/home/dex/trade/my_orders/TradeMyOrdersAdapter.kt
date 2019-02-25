package com.wavesplatform.wallet.v2.ui.home.dex.trade.my_orders

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.model.local.OrderStatus
import com.wavesplatform.wallet.v2.data.model.remote.response.MarketResponse
import com.wavesplatform.wallet.v2.data.model.remote.response.OrderResponse
import com.wavesplatform.wallet.v2.util.stripZeros
import pyxis.uzuki.live.richutilskt.utils.asDateString
import java.math.BigInteger
import java.util.*
import javax.inject.Inject

class TradeMyOrdersAdapter @Inject constructor() : BaseQuickAdapter<OrderResponse, BaseViewHolder>(R.layout.recycle_item_my_orders, null) {
    var market: MarketResponse = MarketResponse()

    override fun convert(helper: BaseViewHolder, item: OrderResponse) {
        helper
                .setText(R.id.text_side, mContext.getString(item.getType().typeUI))
                .setText(R.id.text_price, MoneyUtil.getScaledPrice(item.price, market.amountAssetDecimals, market.priceAssetDecimals).stripZeros())
                .setText(R.id.text_date, item.timestamp.asDateString("dd.MM.yy"))
                .setText(R.id.text_time, item.timestamp.asDateString("HH:mm:ss"))
                .setText(R.id.text_amount, MoneyUtil.getScaledText(item.amount, market.amountAssetDecimals).stripZeros())
                .setText(R.id.text_sum, MoneyUtil.getTextStripZeros(BigInteger.valueOf(item.amount).multiply(BigInteger.valueOf(item.price)).divide(BigInteger.valueOf(100000000)).toLong(), market.priceAssetDecimals).stripZeros())
                .setText(R.id.text_status, mContext.getString(item.getStatus().status))
                .setText(R.id.text_failed_value, MoneyUtil.getTextStripZeros(MoneyUtil.getTextStripZeros(item.filled, market.amountAssetDecimals)))
                .setVisible(R.id.text_failed_value, Arrays.asList(OrderStatus.Filled, OrderStatus.Cancelled, OrderStatus.PartiallyFilled).contains(item.getStatus()))
                .setTextColor(R.id.text_side, item.getType().color)
                .addOnClickListener(R.id.image_delete)
                .setVisible(R.id.relative_cancel_block, !Arrays.asList(OrderStatus.Filled, OrderStatus.Cancelled).contains(item.getStatus()))
                .setTextColor(R.id.text_price, item.getType().color)
    }
}
