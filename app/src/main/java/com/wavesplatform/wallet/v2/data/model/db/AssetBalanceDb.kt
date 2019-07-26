package com.wavesplatform.wallet.v2.data.model.db

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse
import com.wavesplatform.sdk.utils.notNull
import io.realm.RealmModel
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@RealmClass
open class AssetBalanceDb(
        @PrimaryKey
        @SerializedName("assetId") var assetId: String = "",
        @SerializedName("balance") var balance: Long? = 0,
        @SerializedName("leasedBalance") var leasedBalance: Long? = 0,
        @SerializedName("inOrderBalance") var inOrderBalance: Long? = 0,
        @SerializedName("reissuable") var reissuable: Boolean? = false,
        @SerializedName("minSponsoredAssetFee") var minSponsoredAssetFee: Long? = 0,
        @SerializedName("sponsorBalance") var sponsorBalance: Long? = 0,
        @SerializedName("quantity") var quantity: Long? = 0,
        @SerializedName("issueTransaction")
        var issueTransaction: IssueTransactionDb? = IssueTransactionDb(),
        var isHidden: Boolean = false,
        var position: Int = -1,
        @Ignore var configureVisibleState: Boolean = false,
        @Ignore var isChecked: Boolean = false,
        var isFiatMoney: Boolean = false,
        var isFavorite: Boolean = false,
        var isGateway: Boolean = false,
        var isSpam: Boolean = false
) : RealmModel, Parcelable {

    constructor(assetBalance: AssetBalanceResponse?) : this() {
        assetBalance.notNull {
            this.assetId = it.assetId
            this.balance = it.balance
            this.leasedBalance = it.leasedBalance
            this.inOrderBalance = it.inOrderBalance
            this.reissuable = it.reissuable
            this.minSponsoredAssetFee = it.minSponsoredAssetFee
            this.sponsorBalance = it.sponsorBalance
            this.quantity = it.quantity
            this.issueTransaction = IssueTransactionDb(it.issueTransaction)
            this.isHidden = it.isHidden
            this.position = it.position
            this.configureVisibleState = it.configureVisibleState
            this.isChecked = it.isChecked
            this.isFiatMoney = it.isFiatMoney
            this.isFavorite = it.isFavorite
            this.isGateway = it.isGateway
            this.isSpam = it.isSpam
        }
    }

    fun convertFromDb(): AssetBalanceResponse {
        return AssetBalanceResponse(
                assetId = this.assetId,
                balance = this.balance,
                leasedBalance = this.leasedBalance,
                inOrderBalance = this.inOrderBalance,
                reissuable = this.reissuable,
                minSponsoredAssetFee = this.minSponsoredAssetFee,
                sponsorBalance = this.sponsorBalance,
                quantity = this.quantity,
                issueTransaction = this.issueTransaction?.convertFromDb(),
                isHidden = this.isHidden,
                position = this.position,
                configureVisibleState = this.configureVisibleState,
                isChecked = this.isChecked,
                isFiatMoney = this.isFiatMoney,
                isFavorite = this.isFavorite,
                isGateway = this.isGateway,
                isSpam = this.isSpam)
    }

    fun updateInfo(asset: AssetBalanceResponse?) {
        asset?.let {
            this.issueTransaction = IssueTransactionDb(asset.issueTransaction)
        }
    }

    companion object {

        fun convertToDb(assetBalances: List<AssetBalanceResponse>): MutableList<AssetBalanceDb> {
            val list = mutableListOf<AssetBalanceDb>()
            assetBalances.forEach {
                list.add(AssetBalanceDb(it))
            }
            return list
        }

        fun convertFromDb(assetBalances: List<AssetBalanceDb>): MutableList<AssetBalanceResponse> {
            val list = mutableListOf<AssetBalanceResponse>()
            assetBalances.forEach {
                list.add(it.convertFromDb())
            }
            return list
        }
    }
}