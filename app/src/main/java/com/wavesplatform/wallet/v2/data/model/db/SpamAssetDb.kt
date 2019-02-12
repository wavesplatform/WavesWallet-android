package com.wavesplatform.wallet.v2.data.model.db

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.response.AssetBalance
import com.wavesplatform.sdk.model.response.SpamAsset
import com.wavesplatform.wallet.v2.util.notNull
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@RealmClass
open class SpamAssetDb(
        @PrimaryKey
        @SerializedName("assetId") var assetId: String? = "") : RealmModel, Parcelable {

    constructor(spamAsset: SpamAsset?) : this() {
        spamAsset.notNull {
            this.assetId = it.assetId
        }
    }

    fun convertFromDb(): SpamAsset {
        return SpamAsset(assetId = this.assetId)
    }

    companion object {

        fun convertToDb(spamAssets: List<SpamAsset>): List<SpamAssetDb> {
            val list = mutableListOf<SpamAssetDb>()
            spamAssets.forEach {
                list.add(SpamAssetDb(it))
            }
            return list
        }

        fun convertFromDb(spamAssets: List<SpamAssetDb>): List<SpamAsset> {
            val list = mutableListOf<SpamAsset>()
            spamAssets.forEach {
                list.add(it.convertFromDb())
            }
            return list
        }
    }
}