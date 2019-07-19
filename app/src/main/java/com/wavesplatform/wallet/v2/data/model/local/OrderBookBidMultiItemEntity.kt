package com.wavesplatform.wallet.v2.data.model.local

import android.os.Parcelable
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.sdk.model.response.matcher.OrderBookResponse
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.wallet.v2.ui.home.dex.trade.orderbook.TradeOrderBookAdapter
import kotlinx.android.parcel.Parcelize

@Parcelize
class OrderBookBidMultiItemEntity() : OrderBookResponse.BidResponse(), MultiItemEntity, Parcelable {

    constructor(orderBookBid: OrderBookResponse.BidResponse?) : this() {
        orderBookBid.notNull {
            this.amount = it.amount
            this.price = it.price
            this.sum = it.sum
        }
    }

    override fun getItemType(): Int {
        return TradeOrderBookAdapter.BID_TYPE
    }
}