package com.wavesplatform.wallet.v2.data.model.remote.response

import android.os.Parcelable
import com.wavesplatform.wallet.v1.payload.AmountAssetInfo
import com.wavesplatform.wallet.v1.payload.PriceAssetInfo
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Market(
        var id: String? = null,

        var amountAsset: String? = null,
        var amountAssetName: String,
        var priceAsset: String? = null,
        var priceAssetName: String,

        var checked: Boolean = false,
        var verified: Boolean? = false,
        var amountAssetInfo: AmountAssetInfo? = null,
        var priceAssetInfo: PriceAssetInfo? = null,
        var currentTimeFrame: Int? = null
) : Parcelable {
    init {
        this.id = amountAsset + priceAsset
    }
}
