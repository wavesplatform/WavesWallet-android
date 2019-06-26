package com.wavesplatform.sdk.model.response.node.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import kotlinx.android.parcel.Parcelize

@Parcelize
internal class ExchangeTransactionResponse(@SerializedName("order1")
                                  var order1: Order,
                                  @SerializedName("order2")
                                  var order2: Order,
                                  @SerializedName("price")
                                  var price: Long,
                                  @SerializedName("amount")
                                  var amount: Long,
                                  @SerializedName("buyMatcherFee")
                                  var buyMatcherFee: Long,
                                  @SerializedName("sellMatcherFee")
                                  var sellMatcherFee: Long)
    : BaseTransactionResponse(type = BaseTransaction.EXCHANGE), Parcelable {

    @Parcelize
    class Order(
        @SerializedName("id")
        var id: String,
        @SerializedName("sender")
        var sender: String,
        @SerializedName("senderPublicKey")
        var senderPublicKey: String,
        @SerializedName("matcherPublicKey")
        var matcherPublicKey: String,
        @SerializedName("assetPair")
        var assetPair: AssetPair,
        @SerializedName("orderType")
        var orderType: String,
        @SerializedName("price")
        var price: Long,
        @SerializedName("amount")
        var amount: Long,
        @SerializedName("timestamp")
        var timestamp: Long = 0L,
        @SerializedName("expiration")
        var expiration: Long,
        @SerializedName("matcherFee")
        var matcherFee: Long,
        @SerializedName("signature")
        var signature: String?,
        @SerializedName("proofs")
        val proofs: MutableList<String> = mutableListOf()
    ) : Parcelable


    @Parcelize
    class AssetPair(
            @SerializedName("amountAsset")
            var amountAsset: String?,
            @SerializedName("priceAsset")
            var priceAsset: String?
    ) : Parcelable
}