package com.wavesplatform.wallet.v2.data.model.db

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.net.model.response.AssetPair
import com.wavesplatform.sdk.utils.notNull
import io.realm.RealmModel
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize


@Parcelize
@RealmClass
open class AssetPairDb(
        @SerializedName("amountAsset") var amountAsset: String? = "",
        @SerializedName("amountAssetObject") var amountAssetObject: AssetInfoDb? = AssetInfoDb(),
        @SerializedName("priceAsset") var priceAsset: String? = "",
        @SerializedName("priceAssetObject") var priceAssetObject: AssetInfoDb? = AssetInfoDb()
) : RealmModel, Parcelable {

    constructor(alias: AssetPair?) : this() {
        alias.notNull {
            this.amountAsset = it.amountAsset
            this.amountAssetObject = AssetInfoDb(it.amountAssetObject)
            this.priceAsset = it.priceAsset
            this.priceAssetObject = AssetInfoDb(it.priceAssetObject)
        }
    }

    fun convertFromDb(): AssetPair {
        return AssetPair(
                amountAsset = this.amountAsset,
                amountAssetObject = this.amountAssetObject?.convertFromDb(),
                priceAsset = this.priceAsset,
                priceAssetObject = this.priceAssetObject?.convertFromDb())
    }
}