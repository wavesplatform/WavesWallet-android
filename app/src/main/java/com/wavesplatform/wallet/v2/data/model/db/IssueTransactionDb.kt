package com.wavesplatform.wallet.v2.data.model.db

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@RealmClass(name = "IssueTransaction")
open class IssueTransactionDb(
        @SerializedName("type") var type: Int? = 0,
        @PrimaryKey
        @SerializedName("id") var id: String? = "",
        @SerializedName("sender") var sender: String? = "",
        @SerializedName("senderPublicKey") var senderPublicKey: String? = "",
        @SerializedName("fee") var fee: Int? = 0,
        @SerializedName("timestamp") var timestamp: Long? = 0,
        @SerializedName("signature") var signature: String? = "",
        @SerializedName("version") var version: Int? = 0,
        @SerializedName("assetId") var assetId: String? = "",
        @SerializedName("name") var name: String? = "",
        @SerializedName("quantity") var quantity: Long? = 0,
        @SerializedName("reissuable") var reissuable: Boolean? = false,
        @SerializedName("decimals") var decimals: Int? = 0,
        @SerializedName("description") var description: String? = "",
        @SerializedName("script") var script: String? = ""
) : RealmModel, Parcelable