package com.wavesplatform.wallet.v2.data.model.remote.response

import com.google.gson.annotations.SerializedName
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class Transaction(
        @SerializedName("type")
        var type: Int = 0,
        @PrimaryKey
        @SerializedName("id")
        var id: String = "",
        @SerializedName("sender")
        var sender: String = "",
        @SerializedName("senderPublicKey")
        var senderPublicKey: String = "",
        @SerializedName("timestamp")
        var timestamp: Long = 0,
        @SerializedName("amount")
        var amount: Long = 0,
        @SerializedName("signature")
        var signature: String = "",
        @SerializedName("recipient")
        var recipient: String = "",
        @SerializedName("recipientAddress")
        var recipientAddress: String? = "",
        @SerializedName("assetId")
        var assetId: String? = "",
        @SerializedName("leaseId")
        var leaseId: String? = "",
        @SerializedName("alias")
        var alias: String? = "",
        @SerializedName("attachment")
        var attachment: String? = "",
        @SerializedName("status")
        var status: String? = "",
        @SerializedName("lease")
        var lease: Lease? = Lease(),
        @SerializedName("fee")
        var fee: Long = 0,
        @SerializedName("feeAssetId")
        var feeAssetId: String? = null,
        @SerializedName("feeAssetObject")
        var feeAssetObject: AssetInfo? = AssetInfo(),
        @SerializedName("quantity")
        var quantity: Long = 0,
        @SerializedName("price")
        var price: Long = 0,
        @SerializedName("height")
        var height: Long = 0,
        @SerializedName("reissuable")
        var reissuable: Boolean = false,
        @SerializedName("buyMatcherFee")
        var buyMatcherFee: Long = 0,
        @SerializedName("sellMatcherFee")
        var sellMatcherFee: Long = 0,
        @SerializedName("order1")
        var order1: Order? = Order(),
        @SerializedName("order2")
        var order2: Order? = Order(),
        @SerializedName("totalAmount")
        var totalAmount: Long = 0,
        @SerializedName("transfers")
        var transfers: RealmList<Transfer> = RealmList(),
        @SerializedName("data")
        var data: RealmList<Data> = RealmList(),
        @SerializedName("isPending")
        var isPending: Boolean = false,
        @SerializedName("script")
        var script: String? = "",
        @SerializedName("minSponsoredAssetFee")
        var minSponsoredAssetFee: String? = "",
        var transactionTypeId: Int = 0,
        var asset: AssetInfo? = AssetInfo()
) : RealmModel