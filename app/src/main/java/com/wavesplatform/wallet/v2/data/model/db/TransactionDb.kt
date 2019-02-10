package com.wavesplatform.wallet.v2.data.model.db

import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.response.Transaction
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass(name = "Transaction")
open class TransactionDb(
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
        var lease: LeaseDb? = LeaseDb(),
        @SerializedName("fee")
        var fee: Long = 0,
        @SerializedName("feeAssetId")
        var feeAssetId: String? = null,
        @SerializedName("feeAssetObject")
        var feeAssetObject: AssetInfoDb? = AssetInfoDb(),
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
        var order1: OrderDb? = OrderDb(),
        @SerializedName("order2")
        var order2: OrderDb? = OrderDb(),
        @SerializedName("totalAmount")
        var totalAmount: Long = 0,
        @SerializedName("transfers")
        var transfers: RealmList<TransferDb> = RealmList(),
        @SerializedName("data")
        var data: RealmList<Data> = RealmList(),
        @SerializedName("isPending")
        var isPending: Boolean = false,
        @SerializedName("script")
        var script: String? = "",
        @SerializedName("minSponsoredAssetFee")
        var minSponsoredAssetFee: String? = "",
        var transactionTypeId: Int = 0,
        var asset: AssetInfoDb? = AssetInfoDb()
) : RealmModel {

    fun convertFromDb(): Transaction {
        return Transaction()
    }

    companion object {

        fun convertToDb(transaction: Transaction): TransactionDb {
            return TransactionDb()
        }

    }
}