package com.wavesplatform.wallet.v2.data.model.db

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.response.SpamAsset
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@RealmClass(name = "SpamAsset")
open class SpamAssetDb(
        @PrimaryKey
        @SerializedName("assetId") var assetId: String? = "") : RealmModel, Parcelable {


    fun convertFromDb(): SpamAsset {
        return SpamAsset()
    }

    companion object {

        fun convertToDb(spamAsset: SpamAsset): SpamAssetDb {
            return SpamAssetDb()
        }

        fun convertToDb(spamAssets: List<SpamAsset>): List<SpamAssetDb> {
            return listOf()
        }

        fun convertFromDb(spamAssets: List<SpamAssetDb>): List<SpamAsset> {
            return listOf()
        }
    }
}