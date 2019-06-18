/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.response.node

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ScriptInfoResponse(
    @SerializedName("address") var address: String = "",
    @SerializedName("complexity") var complexity: Long = 0L,
    @SerializedName("extraFee") var extraFee: Long = 0L
) : Parcelable