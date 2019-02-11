package com.wavesplatform.wallet.v2.data.model.local

import android.os.Parcelable
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.sdk.model.response.OrderBook
import com.wavesplatform.wallet.v2.ui.home.dex.trade.orderbook.TradeOrderBookAdapter
import kotlinx.android.parcel.Parcelize

@Parcelize
class OrderBookBidMultiItemEntity : OrderBook.Bid(), MultiItemEntity, Parcelable {

    override fun getItemType(): Int {
        return TradeOrderBookAdapter.BID_TYPE
    }
}