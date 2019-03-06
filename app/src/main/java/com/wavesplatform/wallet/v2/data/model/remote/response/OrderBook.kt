package com.wavesplatform.wallet.v2.data.model.remote.response

import android.util.Log
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.google.common.primitives.Bytes
import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.v1.util.SignUtil
import com.wavesplatform.wallet.v2.ui.home.dex.trade.orderbook.TradeOrderBookAdapter

data class OrderBook(
    @SerializedName("timestamp") var timestamp: Long = 0,
    @SerializedName("pair") var pair: Pair = Pair(),
    @SerializedName("bids") var bids: List<Bid> = listOf(),
    @SerializedName("asks") var asks: List<Ask> = listOf()
) {

    data class Pair(
        @SerializedName("amountAsset") var amountAsset: String? = "",
        @SerializedName("priceAsset") var priceAsset: String? = ""
    ) {
        fun toBytes(): ByteArray {
            return try {
                Bytes.concat(SignUtil.arrayOption(amountAsset),
                        SignUtil.arrayOption(priceAsset))
            } catch (e: Exception) {
                Log.e("Wallet", "Couldn't create bytes for AssetPair: ", e)
                ByteArray(0)
            }
        }
    }

    data class Ask(
        @SerializedName("amount") var amount: Long = 0,
        @SerializedName("price") var price: Long = 0,
        @SerializedName("sum") var sum: Double = 0.0
    ) : MultiItemEntity {
        override fun getItemType(): Int {
            return TradeOrderBookAdapter.ASK_TYPE
        }
    }

    data class Bid(
        @SerializedName("amount") var amount: Long = 0,
        @SerializedName("price") var price: Long = 0,
        @SerializedName("sum") var sum: Double = 0.0
    ) : MultiItemEntity {
        override fun getItemType(): Int {
            return TradeOrderBookAdapter.BID_TYPE
        }
    }
}