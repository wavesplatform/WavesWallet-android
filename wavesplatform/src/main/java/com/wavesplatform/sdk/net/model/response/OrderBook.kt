package com.wavesplatform.sdk.net.model.response

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.utils.SignUtil

class OrderBook(
    @SerializedName("timestamp") var timestamp: Long = 0,
    @SerializedName("pair") var pair: Pair = Pair(),
    @SerializedName("bids") var bids: List<Bid> = listOf(),
    @SerializedName("asks") var asks: List<Ask> = listOf()
) {

    class Pair(
        @SerializedName("amountAsset") var amountAsset: String = "",
        @SerializedName("priceAsset") var priceAsset: String = ""
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

    open class Ask(
        @SerializedName("amount") var amount: Long = 0,
        @SerializedName("price") var price: Long = 0,
        @SerializedName("sum") var sum: Double = 0.0
    )

    open class Bid(
        @SerializedName("amount") var amount: Long = 0,
        @SerializedName("price") var price: Long = 0,
        @SerializedName("sum") var sum: Double = 0.0
    )
}