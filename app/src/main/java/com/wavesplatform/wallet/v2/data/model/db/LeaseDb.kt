package com.wavesplatform.wallet.v2.data.model.db

import com.google.gson.annotations.SerializedName
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass(name = "Lease")
open class LeaseDb(
        @SerializedName("type") var type: Int = 0,
        @PrimaryKey
        @SerializedName("id") var id: String = "",
        @SerializedName("sender") var sender: String = "",
        @SerializedName("senderPublicKey") var senderPublicKey: String = "",
        @SerializedName("fee") var fee: Int = 0,
        @SerializedName("timestamp") var timestamp: Long = 0,
        @SerializedName("signature") var signature: String = "",
        @SerializedName("version") var version: Int = 0,
        @SerializedName("amount") var amount: Long = 0,
        @SerializedName("recipient") var recipient: String = ""
) : RealmModel