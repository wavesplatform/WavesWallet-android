package com.wavesplatform.wallet.v2.data.model.remote.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.v1.util.MoneyUtil
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize
import java.util.*


data class AssetBalances(
        @SerializedName("address") var address: String? = null,
        @SerializedName("balances") var balances: List<AssetBalance> = ArrayList()
)

@Parcelize
@RealmClass
open class AssetBalance(
        @PrimaryKey
        @SerializedName("assetId") var assetId: String? = "",
        @SerializedName("balance") var balance: Long? = 0,
        @SerializedName("reissuable") var reissuable: Boolean? = false,
        @SerializedName("minSponsoredAssetFee") var minSponsoredAssetFee: Long? = 0,
        @SerializedName("sponsorBalance") var sponsorBalance: Long? = 0,
        @SerializedName("quantity") var quantity: Long? = 0,
        @SerializedName("issueTransaction") var issueTransaction: IssueTransaction? = IssueTransaction(),
        var isHidden: Boolean = false,
        var position: Int = -1,
        var configureVisibleState: Boolean = false,
        var isFlatMoney: Boolean = false,
        var isFavorite: Boolean = false
) : RealmModel, Parcelable {
    fun getDecimals(): Int? {
        return if (issueTransaction != null) issueTransaction?.decimals else 8
    }

    fun getDisplayBalance(): String {
        return MoneyUtil.getScaledText(balance!!, this)
    }

    fun isAssetId(assetId: String): Boolean {
        return assetId == this.assetId
    }

    fun getName(): String? {
        return issueTransaction?.name
    }

    fun getDisplayBalanceWithUnit(): String {
        return getDisplayBalance() + " " + getName()
    }

    fun isWaves(): Boolean {
        return assetId.isNullOrEmpty()
    }
}

@Parcelize
@RealmClass
open class IssueTransaction(
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