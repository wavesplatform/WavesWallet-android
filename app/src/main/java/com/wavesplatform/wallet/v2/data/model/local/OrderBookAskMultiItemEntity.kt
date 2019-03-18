package com.wavesplatform.wallet.v2.data.model.local

import android.os.Parcelable
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.sdk.net.model.response.OrderBook
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.wallet.v2.ui.home.dex.trade.orderbook.TradeOrderBookAdapter
import kotlinx.android.parcel.Parcelize

@Parcelize
class OrderBookAskMultiItemEntity() : OrderBook.Ask(), MultiItemEntity, Parcelable {

    constructor(orderBookBid: OrderBook.Ask?) : this() {
        orderBookBid.notNull {
            this.amount = it.amount
            this.price = it.price
            this.sum = it.sum
        }
    }

    override fun getItemType(): Int {
        return TradeOrderBookAdapter.ASK_TYPE
    }
}