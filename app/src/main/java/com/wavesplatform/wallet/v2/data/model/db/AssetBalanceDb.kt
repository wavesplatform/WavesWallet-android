package com.wavesplatform.wallet.v2.data.model.db

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.response.AssetBalance
import com.wavesplatform.sdk.model.response.SpamAsset
import io.realm.RealmModel
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@RealmClass(name = "AssetBalance")
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
        @SerializedName("issueTransaction") var issueTransaction: IssueTransactionDb? = IssueTransactionDb(),
        var isHidden: Boolean = false,
        var position: Int = -1,
        @Ignore var configureVisibleState: Boolean = false,
        @Ignore var isChecked: Boolean = false,
        var isFiatMoney: Boolean = false,
        var isFavorite: Boolean = false,
        var isGateway: Boolean = false,
        var isSpam: Boolean = false
) : RealmModel, Parcelable {

        constructor(assetBalance: AssetBalance) : this() {
                //
        }

        fun convertFromDb(): AssetBalance {
                return AssetBalance()
        }

        companion object {

                fun convertToDb(assetBalances: List<AssetBalance>): MutableList<AssetBalanceDb> {
                        return mutableListOf()
                }

                fun convertFromDb(assetBalances: List<AssetBalanceDb>): MutableList<AssetBalance> {
                        return mutableListOf()
                }
        }
}