package com.wavesplatform.sdk.model.response.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.response.node.transaction.AliasTransactionResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AliasDataResponse(
        @SerializedName("__type") var type: String = "alias",
        @SerializedName("data") var alias: AliasTransactionResponse = AliasTransactionResponse()
) : Parcelable