/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.response

import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.utils.WavesConstants
import com.wavesplatform.sdk.model.OrderType

data class LastTradesResponse(
        @SerializedName("__type")
    var type: String = "",
        @SerializedName("data")
    var `data`: List<DataResponse> = listOf(),
        @SerializedName("lastCursor")
    var lastCursor: String = ""
) {
    data class DataResponse(
        @SerializedName("__type")
        var type: String = "",
        @SerializedName("data")
        var transaction: ExchangeTransactionResponse = ExchangeTransactionResponse()
    ) {
        data class ExchangeTransactionResponse(
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
            var order1: ExchangeOrderResponse = ExchangeOrderResponse(),
                @SerializedName("order2")
            var order2: ExchangeOrderResponse = ExchangeOrderResponse(),
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
            data class ExchangeOrderResponse(
                    @SerializedName("amount")
                var amount: Double = 0.0,
                    @SerializedName("assetPair")
                var assetPair: AssetPairResponse = AssetPairResponse(),
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

                data class AssetPairResponse(
                    @SerializedName("amountAsset")
                    var amountAsset: String = "",
                    @SerializedName("priceAsset")
                    var priceAsset: String = ""
                )

                fun getType(): OrderType {
                    return when (orderType) {
                        WavesConstants.BUY_ORDER_TYPE -> OrderType.BUY
                        WavesConstants.SELL_ORDER_TYPE -> OrderType.SELL
                        else -> OrderType.BUY
                    }
                }
            }

            fun getMyOrder(): ExchangeOrderResponse {
                return if (order1.timestamp > order2.timestamp) order1
                else order2
            }
        }
    }
}