package com.wavesplatform.wallet.v2.data.model.remote.response

import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.local.OrderType

data class LastTradesResponse(
    @SerializedName("__type")
    var type: String = "",
    @SerializedName("data")
    var `data`: List<Data> = listOf(),
    @SerializedName("lastCursor")
    var lastCursor: String = ""
) {
    data class Data(
        @SerializedName("__type")
        var type: String = "",
        @SerializedName("data")
        var transaction: ExchangeTransaction = ExchangeTransaction()
    ) {
        data class ExchangeTransaction(
            @SerializedName("amount")
            var amount: Double = 0.0,
            @SerializedName("buyMatcherFee")
            var buyMatcherFee: Double = 0.0,
            @SerializedName("fee")
            var fee: Double = 0.0,
            @SerializedName("height")
            var height: Int = 0,
            @SerializedName("id")
            var id: String = "",
            @SerializedName("order1")
            var order1: ExchangeOrder = ExchangeOrder(),
            @SerializedName("order2")
            var order2: ExchangeOrder = ExchangeOrder(),
            @SerializedName("price")
            var price: Double = 0.0,
            @SerializedName("sellMatcherFee")
            var sellMatcherFee: Double = 0.0,
            @SerializedName("sender")
            var sender: String = "",
            @SerializedName("senderPublicKey")
            var senderPublicKey: String = "",
            @SerializedName("signature")
            var signature: String = "",
            @SerializedName("timestamp")
            var timestamp: String = "",
            @SerializedName("type")
            var type: Int = 0
        ) {
            data class ExchangeOrder(
                @SerializedName("amount")
                var amount: Double = 0.0,
                @SerializedName("assetPair")
                var assetPair: AssetPair = AssetPair(),
                @SerializedName("expiration")
                var expiration: String = "",
                @SerializedName("id")
                var id: String = "",
                @SerializedName("matcherFee")
                var matcherFee: Double = 0.0,
                @SerializedName("matcherPublicKey")
                var matcherPublicKey: String = "",
                @SerializedName("orderType")
                var orderType: String = "",
                @SerializedName("price")
                var price: Double = 0.0,
                @SerializedName("sender")
                var sender: String = "",
                @SerializedName("senderPublicKey")
                var senderPublicKey: String = "",
                @SerializedName("signature")
                var signature: String = "",
                @SerializedName("timestamp")
                var timestamp: String = ""
            ) {

                data class AssetPair(
                    @SerializedName("amountAsset")
                    var amountAsset: String = "",
                    @SerializedName("priceAsset")
                    var priceAsset: String = ""
                )

                fun getType(): OrderType {
                    return when (orderType) {
                        Constants.BUY_ORDER_TYPE -> OrderType.BUY
                        Constants.SELL_ORDER_TYPE -> OrderType.SELL
                        else -> OrderType.BUY
                    }
                }
            }

            fun getMyOrder(): ExchangeOrder {
                return if (order1.timestamp > order2.timestamp) order1
                else order2
            }
        }
    }
}