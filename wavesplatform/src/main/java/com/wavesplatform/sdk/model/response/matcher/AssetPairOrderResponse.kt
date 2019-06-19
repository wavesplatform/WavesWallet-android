/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.response.matcher

import com.google.gson.annotations.SerializedName
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

    fun getScaledPrice(amountAssetDecimals: Int?, priceAssetDecimals: Int?): String {
        return MoneyUtil.getScaledPrice(price,
                amountAssetDecimals ?: 8,
                priceAssetDecimals ?: 8).stripZeros()
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
        const val API_STATUS_CANCELLED = "Cancelled"
        const val API_STATUS_ACCEPTED = "Accepted"
        const val API_STATUS_PARTIALLY_FILLED = "PartiallyFilled"
        const val API_STATUS_FILLED = "Filled"
    }
}
