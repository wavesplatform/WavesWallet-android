package com.wavesplatform.wallet.v2.data.model.db

import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.response.Transaction
import com.wavesplatform.sdk.utils.notNull
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
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
        var data: RealmList<DataDb> = RealmList(),
        @SerializedName("isPending")
        var isPending: Boolean = false,
        @SerializedName("script")
        var script: String? = "",
        @SerializedName("minSponsoredAssetFee")
        var minSponsoredAssetFee: String? = "",
        var transactionTypeId: Int = 0,
        var asset: AssetInfoDb? = AssetInfoDb()
) : RealmModel {

    constructor(transaction: Transaction) : this() {
        transaction.notNull {
            this.type = it.type
            this.id = it.id
            this.sender = it.sender
            this.senderPublicKey = it.senderPublicKey
            this.timestamp = it.timestamp
            this.amount = it.amount
            this.signature = it.signature
            this.recipient = it.recipient
            this.recipientAddress = it.recipientAddress
            this.assetId = it.assetId
            this.leaseId = it.leaseId
            this.alias = it.alias
            this.attachment = it.attachment
            this.status = it.status
            this.lease = LeaseDb(it.lease)
            this.fee = it.fee
            this.feeAssetId = it.feeAssetId
            this.feeAssetObject = AssetInfoDb(it.feeAssetObject)
            this.quantity = it.quantity
            this.price = it.price
            this.height = it.height
            this.reissuable = it.reissuable
            this.buyMatcherFee = it.buyMatcherFee
            this.sellMatcherFee = it.sellMatcherFee
            this.order1 = OrderDb(it.order1)
            this.order2 = OrderDb(it.order2)
            this.totalAmount = it.totalAmount
            this.transfers = TransferDb.convertToDb(it.transfers)
            this.data = DataDb.convertToDb(it.data)
            this.isPending = it.isPending
            this.script = it.script
            this.minSponsoredAssetFee = it.minSponsoredAssetFee
            this.transactionTypeId = it.transactionTypeId
            this.asset = AssetInfoDb(it.asset)
        }
    }

    fun convertFromDb(): Transaction {
        return Transaction(
                type = this.type,
                id = this.id,
                sender = this.sender,
                senderPublicKey = this.senderPublicKey,
                timestamp = this.timestamp,
                amount = this.amount,
                signature = this.signature,
                recipient = this.recipient,
                recipientAddress = this.recipientAddress,
                assetId = this.assetId,
                leaseId = this.leaseId,
                alias = this.alias,
                attachment = this.attachment ?: "",
                status = this.status,
                lease = this.lease?.convertFromDb(),
                fee = this.fee,
                feeAssetId = this.feeAssetId,
                feeAssetObject = this.feeAssetObject?.convertFromDb(),
                quantity = this.quantity,
                price = this.price,
                height = this.height,
                reissuable = this.reissuable,
                buyMatcherFee = this.buyMatcherFee,
                sellMatcherFee = this.sellMatcherFee,
                order1 = this.order1?.convertFromDb(),
                order2 = this.order2?.convertFromDb(),
                totalAmount = this.totalAmount,
                transfers = TransferDb.convertFromDb(this.transfers),
                data = DataDb.convertFromDb(this.data),
                isPending = this.isPending,
                script = this.script,
                minSponsoredAssetFee = this.minSponsoredAssetFee,
                transactionTypeId = this.transactionTypeId,
                asset = this.asset?.convertFromDb())
    }

    companion object {

        fun convertToDb(transactions: List<Transaction>): List<TransactionDb> {
            val list = mutableListOf<TransactionDb>()
            transactions.forEach {
                list.add(TransactionDb(it))
            }
            return list
        }

        fun convertFromDb(transactions: List<TransactionDb>): List<Transaction> {
            val list = mutableListOf<Transaction>()
            transactions.forEach {
                list.add(it.convertFromDb())
            }
            return list
        }
    }
}