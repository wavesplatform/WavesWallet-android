package com.wavesplatform.wallet.v2.ui.home.dex.trade.orderbook

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.OrderbookItem
import com.wavesplatform.wallet.v2.data.model.remote.response.Market
import com.wavesplatform.wallet.v2.ui.home.history.TestObject
import com.wavesplatform.wallet.v2.util.random
import pers.victor.ext.findColor
import javax.inject.Inject

class TradeOrderbookAdapter @Inject constructor() : BaseMultiItemQuickAdapter<OrderbookItem, BaseViewHolder>(null) {

    init {
        addItemType(OrderbookItem.LAST_PRICE_TYPE, R.layout.recycle_item_orderbook_last_price);
        addItemType(OrderbookItem.PRICE_TYPE, R.layout.recycle_item_orderbook);
    }

    override fun convert(helper: BaseViewHolder?, item: OrderbookItem?) {
        when (helper?.getItemViewType()) {
            OrderbookItem.LAST_PRICE_TYPE -> {

            }
            OrderbookItem.PRICE_TYPE -> {
//                if ((0..1).random() == 0) {
//                    helper.setTextColor(R.id.text_price_value, findColor(R.color.error400))
//                }else{
//                    helper.setTextColor(R.id.text_price_value, findColor(R.color.submit400))
//                }
            }
        }

    }
}