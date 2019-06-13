/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.response

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.utils.MoneyUtil
import com.wavesplatform.sdk.utils.SignUtil
import com.wavesplatform.sdk.utils.stripZeros
import java.math.BigInteger

class OrderBookResponse(
        @SerializedName("timestamp") var timestamp: Long = 0,
        @SerializedName("pair") var pair: PairResponse = PairResponse(),
        @SerializedName("bids") var bids: List<BidResponse> = listOf(),
        @SerializedName("asks") var asks: List<AskResponse> = listOf()
) {

    class PairResponse(
            @SerializedName("amountAsset") var amountAsset: String = "",
            @SerializedName("priceAsset") var priceAsset: String = ""
    ) {
        fun toBytes(): ByteArray {
            return try {
                Bytes.concat(SignUtil.arrayOption(amountAsset),
                        SignUtil.arrayOption(priceAsset))
            } catch (e: Exception) {
                Log.e("Wallet", "Couldn't create bytes for AssetPairResponse: ", e)
                ByteArray(0)
            }
        }
    }

    open class AskResponse(
            @SerializedName("amount") var amount: Long = 0,
            @SerializedName("price") var price: Long = 0,
            @SerializedName("sum") var sum: Long = 0
    ) {

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

    open class BidResponse(
            @SerializedName("amount") var amount: Long = 0,
            @SerializedName("price") var price: Long = 0,
            @SerializedName("sum") var sum: Long = 0
    ) {

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