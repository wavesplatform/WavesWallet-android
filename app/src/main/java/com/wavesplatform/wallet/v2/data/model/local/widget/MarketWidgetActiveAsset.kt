/*
 * Created by Eduard Zaydel on 8/8/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local.widget

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.utils.isWaves
import com.wavesplatform.wallet.v2.util.isFiat
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MarketWidgetActiveAsset(
        @SerializedName("name")
        var name: String,
        @SerializedName("id")
        var id: String,
        @SerializedName("amountAsset")
        var amountAsset: String,
        @SerializedName("priceAsset")
        var priceAsset: String,
        @SerializedName("order")
        var order: Int = 0
) : Parcelable {

    companion object {

        fun getMainAssetId(asset: MarketWidgetActiveAsset): String {
            return when {
                asset.priceAsset.isWaves() ->
                    asset.amountAsset
                isFiat(asset.priceAsset) ->
                    asset.amountAsset
                else ->
                    asset.priceAsset
            }
        }
    }
}