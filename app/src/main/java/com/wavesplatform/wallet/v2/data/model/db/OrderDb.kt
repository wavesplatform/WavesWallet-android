package com.wavesplatform.wallet.v2.data.model.db

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.net.model.response.OrderResponse
import com.wavesplatform.sdk.utils.notNull
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@RealmClass
open class OrderDb(
        @PrimaryKey
        @SerializedName("id") var id: String = "",
        @SerializedName("sender") var sender: String = "",
        @SerializedName("senderPublicKey") var senderPublicKey: String = "",
        @SerializedName("matcherPublicKey") var matcherPublicKey: String = "",
        @SerializedName("assetPair") var assetPair: AssetPairDb? = AssetPairDb(),
        @SerializedName("orderType") var orderType: String = "",
        @SerializedName("price") var price: Long = 0,
        @SerializedName("amount") var amount: Long = 0,
        @SerializedName("timestamp") var timestamp: Long = 0,
        @SerializedName("expiration") var expiration: Long = 0,
        @SerializedName("matcherFee") var matcherFee: Long = 0,
        @SerializedName("signature") var signature: String = ""
) : RealmModel, Parcelable {

    constructor(order: OrderResponse?) : this() {
        order.notNull {
            this.id = it.id
            this.sender = it.sender
            this.senderPublicKey = it.senderPublicKey
            this.matcherPublicKey = it.matcherPublicKey
            this.assetPair = AssetPairDb(it.assetPair)
            this.orderType = it.orderType
            this.price = it.price
            this.amount = it.amount
            this.timestamp = it.timestamp
            this.expiration = it.expiration
            this.matcherFee = it.matcherFee
            this.signature = it.signature
        }
    }

    fun convertFromDb(): OrderResponse {
        return OrderResponse(
                id = id,
                sender = sender,
                senderPublicKey = senderPublicKey,
                matcherPublicKey = matcherPublicKey,
                assetPair = assetPair?.convertFromDb(),
                orderType = orderType,
                price = price,
                amount = amount,
                timestamp = timestamp,
                expiration = expiration,
                matcherFee = matcherFee,
                signature = signature)
    }
}