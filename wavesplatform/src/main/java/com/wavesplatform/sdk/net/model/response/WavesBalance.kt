package com.wavesplatform.sdk.net.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class WavesBalance : Parcelable {
    @SerializedName("address") var address: String? = null
    @SerializedName("balance") var balance: Long = 0
}
