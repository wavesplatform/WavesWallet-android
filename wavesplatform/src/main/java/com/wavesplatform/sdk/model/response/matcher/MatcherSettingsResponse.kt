package com.wavesplatform.sdk.model.response.matcher

import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.utils.WavesConstants

class MatcherSettingsResponse(
        @SerializedName("priceAssets") var priceAssets: List<String> = listOf(),
        @SerializedName("orderFee") var orderFee: MutableMap<String, Fee> = hashMapOf()) {

    class Fee(
            @SerializedName("baseFee") var baseFee: Long = WavesConstants.WAVES_ORDER_MIN_FEE,
            @SerializedName("rates") var rates: MutableMap<String, Double> = hashMapOf())

    companion object {
        const val DYNAMIC = "dynamic"
    }
}