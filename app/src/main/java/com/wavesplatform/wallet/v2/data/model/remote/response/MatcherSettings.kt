package com.wavesplatform.wallet.v2.data.model.remote.response

import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.v2.data.Constants

class MatcherSettings(
        @SerializedName("priceAssets") var priceAssets: List<String> = listOf(),
        @SerializedName("orderFee") var orderFee: MutableMap<String, Fee> = hashMapOf()) {

    class Fee(
            @SerializedName("baseFee") var baseFee: Long = Constants.WAVES_ORDER_MIN_FEE,
            @SerializedName("rates") var rates: MutableMap<String, Double> = hashMapOf())

    companion object {
        const val DYNAMIC = "dynamic"
    }
}