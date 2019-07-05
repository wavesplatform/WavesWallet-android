/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.remote.response

import android.util.Log
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.google.common.primitives.Bytes
import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v1.util.SignUtil
import com.wavesplatform.wallet.v2.ui.home.dex.trade.orderbook.TradeOrderBookAdapter
import com.wavesplatform.wallet.v2.util.stripZeros
import java.math.BigInteger

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
            @SerializedName("sum") var sum: Long = 0
    ) : MultiItemEntity {
        override fun getItemType(): Int {
            return TradeOrderBookAdapter.ASK_TYPE
        }

        val total: Long
            get() {
                return BigInteger.valueOf(amount)
                        .multiply(BigInteger.valueOf(price))
                        .divide(BigInteger.valueOf(100000000)).toLong()
            }

        fun getScaledSum(priceAssetDecimals: Int?): String {
            return MoneyUtil.getTextStripZeros(sum, priceAssetDecimals ?: 8).stripZeros()
        }
    }

    data class Bid(
            @SerializedName("amount") var amount: Long = 0,
            @SerializedName("price") var price: Long = 0,
            @SerializedName("sum") var sum: Long = 0
    ) : MultiItemEntity {
        override fun getItemType(): Int {
            return TradeOrderBookAdapter.BID_TYPE
        }

        val total: Long
            get() {
                return BigInteger.valueOf(amount)
                        .multiply(BigInteger.valueOf(price))
                        .divide(BigInteger.valueOf(100000000)).toLong()
            }


        fun getScaledSum(priceAssetDecimals: Int?): String {
            return MoneyUtil.getTextStripZeros(sum, priceAssetDecimals ?: 8).stripZeros()
        }
    }
}