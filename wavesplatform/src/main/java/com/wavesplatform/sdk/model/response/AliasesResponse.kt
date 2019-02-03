package com.wavesplatform.sdk.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


data class AliasesResponse(
        @SerializedName("__type") var type: String = "list",
        @SerializedName("data") var data: List<AliasData> = listOf()
)

data class AliasData(
        @SerializedName("__type") var type: String = "alias",
        @SerializedName("data") var alias: Alias = Alias()
)

@Parcelize
open class Alias(
        @SerializedName("alias") var alias: String? = "",
        @SerializedName("address") var address: String? = "",
        var own: Boolean = false
) : Parcelable