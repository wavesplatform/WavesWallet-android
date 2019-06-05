/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex.trade.my_orders

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.sdk.model.OrderStatus
import com.wavesplatform.wallet.R
import com.wavesplatform.sdk.model.response.MarketResponse
import com.wavesplatform.sdk.model.response.AssetPairOrderResponse
import com.wavesplatform.wallet.v2.ui.home.dex.trade.my_orders.details.MyOrderDetailsBottomSheetFragment.Companion.FILLED_ORDER_PERCENT
import kotlinx.android.synthetic.main.item_my_orders.view.*
import pyxis.uzuki.live.richutilskt.utils.asDateString
import javax.inject.Inject
import kotlin.math.roundToInt

class TradeMyOrdersAdapter @Inject constructor() : BaseQuickAdapter<AssetPairOrderResponse, BaseViewHolder>(R.layout.item_my_orders, null) {
    var market: MarketResponse = MarketResponse()

    override fun convert(helper: BaseViewHolder, item: AssetPairOrderResponse) {
        helper
                .setText(R.id.text_side, mContext.getString(item.getType().typeUI))
                .setText(R.id.text_price, item.getScaledPrice(market.amountAssetDecimals, market.priceAssetDecimals))
                .setText(R.id.text_date, item.timestamp.asDateString("dd.MM.yy"))
                .setText(R.id.text_time, item.timestamp.asDateString("HH:mm:ss"))
                .setText(R.id.text_status, mContext.getString(item.getStatus().status))
                .setTextColor(R.id.text_side, item.getType().color)
                .setTextColor(R.id.text_price, item.getType().color)

        makeTransparentIfCanceled(helper.itemView, item.getStatus())

        val percent = (item.filled.toFloat() / item.amount.toFloat()) * 100
        when (item.getStatus()) {
            OrderStatus.Filled -> {
                // force string, bcz percent with commission
                helper.itemView.text_failed_value?.text = FILLED_ORDER_PERCENT
            }
            else -> {
                // with template "{percent}%"
                helper.itemView.text_failed_value?.text = percent
                        .roundToInt()
                        .toString()
                        .plus("%")
            }
        }
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
