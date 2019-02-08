package com.wavesplatform.wallet.v2.data.model.remote.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AmountAssetInfo(
        @SerializedName("decimals") var decimals: Int = 0
) : Parcelable