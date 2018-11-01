package com.wavesplatform.wallet.v2.data.model.remote.response

import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.v2.data.model.local.LastTradeType


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
    fun getType(): LastTradeType {
        return when (type) {
            LastTradeType.BUY.type -> LastTradeType.BUY
            LastTradeType.SELL.type -> LastTradeType.SELL
            else -> LastTradeType.BUY
        }
    }
}