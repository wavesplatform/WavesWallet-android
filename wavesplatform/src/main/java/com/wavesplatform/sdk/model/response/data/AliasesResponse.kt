/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.response.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.response.node.transaction.AliasTransactionResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AliasesResponse(
        @SerializedName("__type") var type: String = "list",
        @SerializedName("data") var data: List<AliasDataResponse> = listOf()
) : Parcelable

@Parcelize
data class AliasDataResponse(
        @SerializedName("__type") var type: String = "alias",
        @SerializedName("data") var alias: AliasTransactionResponse = AliasTransactionResponse()
) : Parcelable