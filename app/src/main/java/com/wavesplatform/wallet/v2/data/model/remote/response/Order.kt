package com.wavesplatform.wallet.v2.data.model.remote.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@RealmClass
open class Order(
        @PrimaryKey
        @SerializedName("id") var id: String = "",
        @SerializedName("sender") var sender: String = "",
        @SerializedName("senderPublicKey") var senderPublicKey: String = "",
        @SerializedName("matcherPublicKey") var matcherPublicKey: String = "",
        @SerializedName("assetPair") var assetPair: AssetPair? = AssetPair(),
        @SerializedName("orderType") var orderType: String = "",
        @SerializedName("price") var price: Long = 0,
        @SerializedName("amount") var amount: Long = 0,
        @SerializedName("timestamp") var timestamp: Long = 0,
        @SerializedName("expiration") var expiration: Long = 0,
        @SerializedName("matcherFee") var matcherFee: Long = 0,
        @SerializedName("signature") var signature: String = ""
) : RealmModel, Parcelable