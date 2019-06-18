/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.response.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal

@Parcelize
data class PairResponse(
    @SerializedName("__type") var type: String = "",
    @SerializedName("data") var data: DataResponse = DataResponse()
) : Parcelable {

    @Parcelize
    data class DataResponse(
        @SerializedName("firstPrice") var firstPrice: BigDecimal = BigDecimal(0),
        @SerializedName("lastPrice") var lastPrice: BigDecimal = BigDecimal(0),
        @SerializedName("volume") var volume: BigDecimal = BigDecimal(0),
        @SerializedName("volumeWaves") var volumeWaves: BigDecimal? = BigDecimal(0)
    ) : Parcelable
}