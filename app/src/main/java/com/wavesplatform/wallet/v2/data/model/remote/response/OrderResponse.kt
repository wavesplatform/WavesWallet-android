package com.wavesplatform.wallet.v2.data.model.remote.response

import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.v2.data.model.local.OrderStatus

class OrderResponse {

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
    private val status: String = ""
    @SerializedName("assetPair")
    var assetPair: AssetPair? = null

    class AssetPair {
        @SerializedName("amountAsset")
        var amountAsset: String = ""
        @SerializedName("priceAsset")
        var priceAsset: String = ""
    }

    fun getStatus(): OrderStatus {
        return when (status) {
            "Accepted" -> OrderStatus.Accepted
            "PartiallyFilled" -> OrderStatus.PartiallyFilled
            "Cancelled" -> OrderStatus.Cancelled
            "Filled" -> OrderStatus.Filled
            else -> OrderStatus.Filled
        }
    }
}

