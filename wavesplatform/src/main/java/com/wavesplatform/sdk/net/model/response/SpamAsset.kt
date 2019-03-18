package com.wavesplatform.sdk.net.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
open class SpamAsset(@SerializedName("assetId") var assetId: String? = "") : Parcelable