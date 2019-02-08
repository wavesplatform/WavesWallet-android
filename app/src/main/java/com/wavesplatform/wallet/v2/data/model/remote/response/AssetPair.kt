package com.wavesplatform.wallet.v2.data.model.remote.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmModel
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize

@RealmClass
@Parcelize
open class AssetPair(
        @SerializedName("amountAsset") var amountAsset: String? = "",
        @SerializedName("amountAssetObject") var amountAssetObject: AssetInfo? = AssetInfo(),
        @SerializedName("priceAsset") var priceAsset: String? = "",
        @SerializedName("priceAssetObject") var priceAssetObject: AssetInfo? = AssetInfo()
) : RealmModel, Parcelable