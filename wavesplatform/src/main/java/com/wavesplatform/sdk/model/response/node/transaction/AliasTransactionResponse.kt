package com.wavesplatform.sdk.model.response.node.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import kotlinx.android.parcel.Parcelize

@Parcelize
open class AliasTransactionResponse() : Parcelable {

    @SerializedName("type")
    val type: Int = BaseTransaction.CREATE_ALIAS
    @SerializedName("timestamp")
    var timestamp: Long = 0L
    @SerializedName("fee")
    var fee: Long = 0L
    @SerializedName("version")
    var version: Int = 0
    @SerializedName("proofs")
    val proofs: MutableList<String> = mutableListOf()
    @SerializedName("id")
    var id: String? = null
    @SerializedName("sender")
    var sender: String = ""
    @SerializedName("senderPublicKey")
    var senderPublicKey: String = ""
    @SerializedName("height")
    var height: Long = 0L
    @SerializedName("signature")
    var signature: String = ""


    @SerializedName("alias")
    var alias: String? = ""
    @SerializedName("address")
    var address: String? = ""
    var own: Boolean = false

    constructor(alias: String? = "", address: String? = "", own: Boolean = false) : this() {
        this.alias = alias
        this.address = address
        this.own = own
    }
}