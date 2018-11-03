package com.wavesplatform.wallet.v2.data.model.remote.response

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.v2.ui.home.dex.trade.orderbook.TradeOrderBookAdapter


data class OrderBook(
        @SerializedName("timestamp") var timestamp: Long = 0,
        @SerializedName("pair") var pair: Pair = Pair(),
        @SerializedName("bids") var bids: List<Bid> = listOf(),
        @SerializedName("asks") var asks: List<Ask> = listOf()
) {

    data class Pair(
            @SerializedName("amountAsset") var amountAsset: String = "",
            @SerializedName("priceAsset") var priceAsset: String = ""
    )


    data class Ask(
            @SerializedName("amount") var amount: Long = 0,
            @SerializedName("price") var price: Long = 0,
            @SerializedName("width") var width: Float = 0f
    ) : MultiItemEntity {
        override fun getItemType(): Int {
            return TradeOrderBookAdapter.ASK_TYPE
        }
    }


    data class Bid(
            @SerializedName("amount") var amount: Long = 0,
            @SerializedName("price") var price: Long = 0,
            @SerializedName("width") var width: Float = 0f
    ) : MultiItemEntity {
        override fun getItemType(): Int {
            return TradeOrderBookAdapter.BID_TYPE
        }
    }
}