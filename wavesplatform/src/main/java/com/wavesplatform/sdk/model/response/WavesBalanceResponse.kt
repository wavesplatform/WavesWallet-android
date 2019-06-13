/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class WavesBalanceResponse : Parcelable {
    @SerializedName("address") var address: String? = null
    @SerializedName("balance") var balance: Long = 0
}
