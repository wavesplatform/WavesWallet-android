package com.wavesplatform.wallet.v2.data.model.remote.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AssetsDetails(
    @SerializedName("assetId") var assetId: String = "",
    @SerializedName("issueHeight") var issueHeight: Long = 0L,
    @SerializedName("issueTimestamp") var issueTimestamp: Long = 0L,
    @SerializedName("issuer") var issuer: String = "",
    @SerializedName("name") var name: String = "",
    @SerializedName("description") var description: String = "",
    @SerializedName("decimals") var decimals: Int = 8,
    @SerializedName("reissuable") var reissuable: Boolean? = null,
    @SerializedName("quantity") var quantity: Long = 0L,
    @SerializedName("scripted") var scripted: Boolean = false,
    @SerializedName("minSponsoredAssetFee") var minSponsoredAssetFee: Long = 0L
) : Parcelable