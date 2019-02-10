package com.wavesplatform.wallet.v2.data.model.db

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AmountAssetInfoDb(@SerializedName("decimals") var decimals: Int = 0) : Parcelable