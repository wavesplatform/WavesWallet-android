package com.wavesplatform.wallet.v2.data.model.db

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmModel
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@RealmClass
open class AssetBalance(
        @PrimaryKey
        @SerializedName("assetId") var assetId: String = "",
        @SerializedName("balance") var balance: Long? = 0,
        @SerializedName("leasedBalance") var leasedBalance: Long? = 0,
        @SerializedName("inOrderBalance") var inOrderBalance: Long? = 0,
        @SerializedName("reissuable") var reissuable: Boolean? = false,
        @SerializedName("minSponsoredAssetFee") var minSponsoredAssetFee: Long? = 0,
        @SerializedName("sponsorBalance") var sponsorBalance: Long? = 0,
        @SerializedName("quantity") var quantity: Long? = 0,
        @SerializedName("issueTransaction") var issueTransaction: IssueTransaction? = IssueTransaction(),
        var isHidden: Boolean = false,
        var position: Int = -1,
        @Ignore var configureVisibleState: Boolean = false,
        @Ignore var isChecked: Boolean = false,
        var isFiatMoney: Boolean = false,
        var isFavorite: Boolean = false,
        var isGateway: Boolean = false,
        var isSpam: Boolean = false
) : RealmModel, Parcelable