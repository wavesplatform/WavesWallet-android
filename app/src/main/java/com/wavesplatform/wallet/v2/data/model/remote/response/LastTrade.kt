package com.wavesplatform.wallet.v2.data.model.remote.response

import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.v2.data.model.local.OrderType
import com.wavesplatform.wallet.v2.data.model.remote.response.OrderResponse.Companion.API_BUY_TYPE
import com.wavesplatform.wallet.v2.data.model.remote.response.OrderResponse.Companion.API_SELL_TYPE


data class LastTrade(
        @SerializedName("timestamp") var timestamp: Long = 0,
        @SerializedName("id") var id: String = "",
        @SerializedName("confirmed") var confirmed: Boolean = false,
        @SerializedName("type") var type: String = "",
        @SerializedName("price") var price: String = "",
        @SerializedName("amount") var amount: String = "",
        @SerializedName("buyer") var buyer: String = "",
        @SerializedName("seller") var seller: String = "",
        @SerializedName("matcher") var matcher: String = ""
) {
    fun getType(): OrderType {
        return when (type) {
            API_BUY_TYPE -> OrderType.BUY
            API_SELL_TYPE -> OrderType.SELL
            else -> OrderType.BUY
        }
    }

    companion object {
        var API_BUY_TYPE = "buy"
        var API_SELL_TYPE = "sell"
    }
}