package com.wavesplatform.wallet.v2.data.model.remote.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal

@Parcelize
data class PairResponse(
    @SerializedName("__type") var type: String = "",
    @SerializedName("data") var data: Data = Data()
) : Parcelable {

    @Parcelize
    data class Data(
        @SerializedName("firstPrice") var firstPrice: BigDecimal = BigDecimal(0),
        @SerializedName("lastPrice") var lastPrice: BigDecimal = BigDecimal(0),
        @SerializedName("volume") var volume: BigDecimal = BigDecimal(0),
        @SerializedName("volumeWaves") var volumeWaves: BigDecimal? = BigDecimal(0)
    ) : Parcelable
}