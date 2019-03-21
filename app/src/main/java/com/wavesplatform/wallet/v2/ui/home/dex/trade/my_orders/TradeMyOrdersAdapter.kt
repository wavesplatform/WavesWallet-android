package com.wavesplatform.wallet.v2.ui.home.dex.trade.my_orders

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.sdk.net.model.OrderStatus
import com.wavesplatform.wallet.R
import com.wavesplatform.sdk.net.model.response.MarketResponse
import com.wavesplatform.sdk.net.model.response.OrderResponse
import kotlinx.android.synthetic.main.recycle_item_my_orders.view.*
import pyxis.uzuki.live.richutilskt.utils.asDateString
import java.util.*
import javax.inject.Inject

class TradeMyOrdersAdapter @Inject constructor() : BaseQuickAdapter<OrderResponse, BaseViewHolder>(R.layout.recycle_item_my_orders, null) {
    var market: MarketResponse = MarketResponse()

    override fun convert(helper: BaseViewHolder, item: OrderResponse) {
        helper
                .setText(R.id.text_side, mContext.getString(item.getType().typeUI))
                .setText(R.id.text_price, item.getScaledPrice(market.amountAssetDecimals, market.priceAssetDecimals))
                .setText(R.id.text_date, item.timestamp.asDateString("dd.MM.yy"))
                .setText(R.id.text_time, item.timestamp.asDateString("HH:mm:ss"))
                .setText(R.id.text_status, mContext.getString(item.getStatus().status))
                .setText(R.id.text_failed_value, item.getScaledFilled(market.amountAssetDecimals))
                .setVisible(R.id.text_failed_value, Arrays.asList(OrderStatus.Filled, OrderStatus.Cancelled, OrderStatus.PartiallyFilled).contains(item.getStatus()))
                .setTextColor(R.id.text_side, item.getType().color)
                .addOnClickListener(R.id.image_delete)
                .setTextColor(R.id.text_price, item.getType().color)

        makeTransparentIfCanceled(helper.itemView, item.getStatus())
    }

    private fun makeTransparentIfCanceled(view: View, status: OrderStatus) {
        if (status == OrderStatus.Cancelled) {
            view.linear_time.alpha = 0.3f
            view.linear_status.alpha = 0.3f
            view.linear_type.alpha = 0.3f
        }else{
            view.linear_time.alpha = 1f
            view.linear_status.alpha = 1f
            view.linear_type.alpha = 1f
        }
    }
}
