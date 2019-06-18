/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.response.matcher

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
open class MarketResponse(
        @SerializedName("id") var id: String? = "",
        @SerializedName("amountAsset") var amountAsset: String = "",
        @SerializedName("amountAssetName") var amountAssetName: String = "",
        @SerializedName("amountAssetShortName") var amountAssetShortName: String? = "",
        @SerializedName("amountAssetLongName") var amountAssetLongName: String? = "",
        @SerializedName("amountAssetDecimals") var amountAssetDecimals: Int = 0,
        @SerializedName("amountAssetInfo") var amountAssetInfo: AmountAssetInfoResponse = AmountAssetInfoResponse(),
        @SerializedName("priceAsset") var priceAsset: String = "",
        @SerializedName("priceAssetName") var priceAssetName: String = "",
        @SerializedName("priceAssetShortName") var priceAssetShortName: String? = "",
        @SerializedName("priceAssetLongName") var priceAssetLongName: String? = "",
        @SerializedName("priceAssetInfo") var priceAssetInfo: PriceAssetInfoResponse = PriceAssetInfoResponse(),
        @SerializedName("priceAssetDecimals") var priceAssetDecimals: Int = 0,
        @SerializedName("created") var created: Long = 0,
        @SerializedName("checked") var checked: Boolean = false,
        @SerializedName("popular") var popular: Boolean = false,
        @SerializedName("position") var position: Int = -1,
        @SerializedName("currentTimeFrame") var currentTimeFrame: Int? = null
) : Parcelable

@Parcelize
data class AmountAssetInfoResponse(
        @SerializedName("decimals") var decimals: Int = 0
) : Parcelable

@Parcelize
data class PriceAssetInfoResponse(
        @SerializedName("decimals") var decimals: Int = 0
) : Parcelable