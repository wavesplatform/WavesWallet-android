package com.wavesplatform.wallet.v2.data.model.db

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.response.IssueTransaction
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.sdk.utils.notNull
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@RealmClass
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
) : RealmModel, Parcelable {

    constructor(transaction: IssueTransaction?) : this() {
        transaction.notNull {
            this.type = it.type
            this.id = it.id
            this.sender = it.sender
            this.senderPublicKey = it.senderPublicKey
            this.fee = it.fee
            this.timestamp = it.timestamp
            this.signature = it.signature
            this.version = it.version
            this.assetId = it.assetId
            this.name = it.name
            this.quantity = it.quantity
            this.reissuable = it.reissuable
            this.decimals = it.decimals
            this.description = it.description
            this.script = it.script
        }
    }

    fun convertFromDb(): IssueTransaction {
        return IssueTransaction(
                type = this.type,
                id = this.id,
                sender = this.sender,
                senderPublicKey = this.senderPublicKey,
                fee = this.fee,
                timestamp = this.timestamp,
                signature = this.signature,
                version = this.version,
                assetId = this.assetId,
                name = this.name,
                quantity = this.quantity,
                reissuable = this.reissuable,
                decimals = this.decimals,
                description = this.description,
                script = this.script
        )
    }
}