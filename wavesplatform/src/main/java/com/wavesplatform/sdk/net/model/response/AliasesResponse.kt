/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.net.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AliasesResponse(
    @SerializedName("__type") var type: String = "list",
    @SerializedName("data") var data: List<AliasData> = listOf()
) : Parcelable

@Parcelize
data class AliasData(
    @SerializedName("__type") var type: String = "alias",
    @SerializedName("data") var alias: Alias = Alias()
) : Parcelable

@Parcelize
open class Alias(
    @SerializedName("alias") var alias: String? = "",
    @SerializedName("address") var address: String? = "",
    var own: Boolean = false
) : Parcelable