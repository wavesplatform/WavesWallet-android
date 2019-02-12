package com.wavesplatform.wallet.v2.data.model.db

import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.response.Alias
import com.wavesplatform.sdk.model.response.Lease
import com.wavesplatform.wallet.v2.util.notNull
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
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
) : RealmModel {

    constructor(lease: Lease?) : this() {
        lease.notNull {
            this.id = it.id
            this.sender = it.sender
            this.senderPublicKey = it.senderPublicKey
            this.fee = it.fee
            this.timestamp = it.timestamp
            this.signature = it.signature
            this.version = it.version
            this.amount = it.amount
            this.recipient = it.recipient
        }
    }

    fun convertFromDb(): Lease {
        return Lease(
                id = this.id,
                sender = this.sender,
                senderPublicKey = this.senderPublicKey,
                fee = this.fee,
                timestamp = this.timestamp,
                signature = this.signature,
                version = this.version,
                amount = this.amount,
                recipient = this.recipient)
    }
}