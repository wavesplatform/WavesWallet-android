package com.wavesplatform.wallet.v2.data.model.remote.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Market(
        var id: String? = null,

        @SerializedName("amountAsset") var amountAsset: String = "",
        @SerializedName("amountAssetName") var amountAssetName: String = "",
        @SerializedName("amountAssetShortName") var amountAssetShortName: String? = "",
        @SerializedName("amountAssetLongName") var amountAssetLongName: String? = "",
        @SerializedName("amountAssetInfo") var amountAssetInfo: AmountAssetInfo = AmountAssetInfo(),
        @SerializedName("priceAsset") var priceAsset: String = "",
        @SerializedName("priceAssetName") var priceAssetName: String = "",
        @SerializedName("priceAssetShortName") var priceAssetShortName: String? = "",
        @SerializedName("priceAssetLongName") var priceAssetLongName: String? = "",
        @SerializedName("priceAssetInfo") var priceAssetInfo: PriceAssetInfo = PriceAssetInfo(),
        @SerializedName("created") var created: Long = 0,
        var checked: Boolean = false
) : Parcelable

@Parcelize
data class AmountAssetInfo(
        @SerializedName("decimals") var decimals: Int = 0
) : Parcelable

@Parcelize
data class PriceAssetInfo(
        @SerializedName("decimals") var decimals: Int = 0
) : Parcelable