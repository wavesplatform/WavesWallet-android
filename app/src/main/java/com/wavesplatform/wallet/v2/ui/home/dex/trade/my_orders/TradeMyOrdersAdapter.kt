package com.wavesplatform.wallet.v2.ui.home.dex.trade.my_orders

import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.model.local.MyOrderItem
import com.wavesplatform.wallet.v2.data.model.remote.response.MarketResponse
import pers.victor.ext.findColor
import pyxis.uzuki.live.richutilskt.utils.asDateString
import javax.inject.Inject

class TradeMyOrdersAdapter @Inject constructor() : BaseSectionQuickAdapter<MyOrderItem, BaseViewHolder>(R.layout.recycle_item_my_orders, R.layout.header_my_orders, null) {
    var market: MarketResponse = MarketResponse()

    override fun convertHead(helper: BaseViewHolder?, item: MyOrderItem?) {
        helper?.setText(R.id.text_header_text, item?.header)
    }

    override fun convert(helper: BaseViewHolder, item: MyOrderItem) {
        helper.addOnClickListener(R.id.image_delete)
                .setTextColor(R.id.text_status_value,
                        if (item.t.amount == item.t.filled) findColor(R.color.submit400)
                        else findColor(R.color.error400))
                .setText(R.id.text_status_value, item.t.getStatus().status)
                .setText(R.id.text_time_value, item.t.timestamp.asDateString("HH:mm:ss"))
                .setText(R.id.text_price_value, MoneyUtil.getScaledText(item.t.price, market.priceAssetDecimals))
                .setText(R.id.text_amount_value, MoneyUtil.getScaledText(item.t.amount, market.amountAssetDecimals))
    }
}

