package com.wavesplatform.wallet.v2.data.model.remote.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ScriptInfo(
    @SerializedName("address") var address: String = "",
    @SerializedName("complexity") var complexity: Long = 0L,
    @SerializedName("extraFee") var extraFee: Long = 0L
) : Parcelable