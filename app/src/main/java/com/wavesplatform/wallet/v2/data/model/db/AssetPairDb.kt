package com.wavesplatform.wallet.v2.data.model.db

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmModel
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize


@Parcelize
@RealmClass(name = "AssetPair")
open class AssetPairDb(
        @SerializedName("amountAsset") var amountAsset: String? = "",
        @SerializedName("amountAssetObject") var amountAssetObject: AssetInfoDb? = AssetInfoDb(),
        @SerializedName("priceAsset") var priceAsset: String? = "",
        @SerializedName("priceAssetObject") var priceAssetObject: AssetInfoDb? = AssetInfoDb()
) : RealmModel, Parcelable