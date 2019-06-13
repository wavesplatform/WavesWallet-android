/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.response

import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.OrderStatus
import com.wavesplatform.sdk.model.OrderType
import com.wavesplatform.sdk.utils.MoneyUtil
import com.wavesplatform.sdk.utils.stripZeros
import java.math.BigInteger

class AssetPairOrderResponse {

    @SerializedName("id")
    var id: String = ""
    @SerializedName("type")
    var type: String = ""
    @SerializedName("amount")
    var amount: Long = 0
    @SerializedName("price")
    var price: Long = 0
    @SerializedName("timestamp")
    var timestamp: Long = 0
    @SerializedName("filled")
    var filled: Long = 0
    @SerializedName("status")
    var status: String = ""
    @SerializedName("assetPair")
    var assetPair: AssetPairResponse? = null
    @SerializedName("sectionTimestamp")
    var sectionTimestamp: Long = 0

    class AssetPairResponse {
        @SerializedName("amountAsset")
        var amountAsset: String = ""
        @SerializedName("priceAsset")
        var priceAsset: String = ""
    }

    fun getType(): OrderType {
        return when (type) {
            API_BUY_TYPE -> OrderType.BUY
            API_SELL_TYPE -> OrderType.SELL
            else -> OrderType.BUY
        }
    }

    fun getStatus(): OrderStatus {
        return when (status) {
            API_STATUS_ACCEPTED -> OrderStatus.Accepted
            API_STATUS_PARTIALLY_FILLED -> OrderStatus.PartiallyFilled
            API_STATUS_CANCELLED -> OrderStatus.Cancelled
            API_STATUIS_FILLED -> OrderStatus.Filled
            else -> OrderStatus.Filled
        }
    }

    fun getScaledPrice(amountAssetDecimals: Int?, priceAssetDecimals: Int?): String {
        return MoneyUtil.getScaledPrice(price,
                amountAssetDecimals ?: 8,
                priceAssetDecimals ?: 8).stripZeros()
    }

    fun getScaledFilled(amountAssetDecimals: Int?): String {
        val notScaledValue = if (getStatus() == OrderStatus.Filled) {
            amount
        } else {
            filled
        }
        return MoneyUtil.getTextStripZeros(MoneyUtil.getTextStripZeros(notScaledValue,
                amountAssetDecimals ?: 8))
    }

    fun getScaledTotal(priceAssetDecimals: Int?): String {
        return MoneyUtil.getTextStripZeros(
                BigInteger.valueOf(amount)
                        .multiply(BigInteger.valueOf(price))
                        .divide(BigInteger.valueOf(100000000)).toLong(),
                priceAssetDecimals ?: 8).stripZeros()
    }

    fun getScaledAmount(amountAssetDecimals: Int?): String {
        return MoneyUtil.getScaledText(amount, amountAssetDecimals ?: 8).stripZeros()
    }

    companion object {
        const val API_BUY_TYPE = "buy"
        const val API_SELL_TYPE = "sell"

        const val API_STATUS_CANCELLED = "Cancelled"
        const val API_STATUS_ACCEPTED = "Accepted"
        const val API_STATUS_PARTIALLY_FILLED = "PartiallyFilled"
        const val API_STATUIS_FILLED = "Filled"
    }
}
