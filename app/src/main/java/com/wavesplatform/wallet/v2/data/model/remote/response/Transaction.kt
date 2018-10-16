package com.wavesplatform.wallet.v2.data.model.remote.response

import com.google.common.base.Optional
import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.v1.api.NodeManager
import com.wavesplatform.wallet.v1.util.MoneyUtil
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import org.apache.commons.lang3.ArrayUtils

@RealmClass
open class Lease(
        @SerializedName("type") var type: Int = 0,
        @PrimaryKey
        @SerializedName("id") var id: String = "",
        @SerializedName("sender") var sender: String = "",
        @SerializedName("senderPublicKey") var senderPublicKey: String = "",
        @SerializedName("fee") var fee: Int = 0,
        @SerializedName("timestamp") var timestamp: Long = 0,
        @SerializedName("signature") var signature: String = "",
        @SerializedName("version") var version: Int = 0,
        @SerializedName("amount") var amount: Int = 0,
        @SerializedName("recipient") var recipient: String = ""
) : RealmModel

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
) : RealmModel

@RealmClass
open class AssetPair(
        @SerializedName("amountAsset") var amountAsset: String? = "",
        @SerializedName("amountAssetObject") var amountAssetObject: AssetInfo? = AssetInfo(),
        @SerializedName("priceAsset") var priceAsset: String? = "",
        @SerializedName("priceAssetObject") var priceAssetObject: AssetInfo? = AssetInfo()
) : RealmModel

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
        var transactionTypeId: Int = 0,
        var asset: AssetInfo? = AssetInfo()
) : RealmModel {

    val displayAmount: String
        get() = MoneyUtil.getDisplayWaves(amount)

    val decimals: Int
        get() = 8


    val assetName: String
        get() = "WAVES"

    val conterParty: Optional<String>
        get() = Optional.absent()

    val isOwn: Boolean
        get() = ArrayUtils.isEquals(NodeManager.get().address, sender)


    fun isForAsset(assetId: String): Boolean {
        return false
    }

    fun toBytes(): ByteArray {
        return ArrayUtils.EMPTY_BYTE_ARRAY
    }

}

@RealmClass
open class Data(
        @SerializedName("key") var key: String = "",
        @SerializedName("type") var type: String = "",
        @SerializedName("value") var value: String = ""
) : RealmModel

@RealmClass
open class Transfer(
        @SerializedName("recipient")
        var recipient: String = "",
        @SerializedName("recipientAddress")
        var recipientAddress: String? = "",
        @SerializedName("amount")
        var amount: Long = 0
) : RealmModel