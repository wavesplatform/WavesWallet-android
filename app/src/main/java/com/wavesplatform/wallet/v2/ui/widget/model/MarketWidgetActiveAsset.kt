/*
 * Created by Eduard Zaydel on 25/7/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
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
) : Parcelable