package com.wavesplatform.wallet.v2.data.model.db

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.net.model.response.SpamAssetResponse
import com.wavesplatform.sdk.utils.notNull
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@RealmClass
open class SpamAssetDb(
        @PrimaryKey
        @SerializedName("assetId") var assetId: String? = "") : RealmModel, Parcelable {

    constructor(spamAsset: SpamAssetResponse?) : this() {
        spamAsset.notNull {
            this.assetId = it.assetId
        }
    }

    fun convertFromDb(): SpamAssetResponse {
        return SpamAssetResponse(assetId = this.assetId)
    }

    companion object {

        fun convertToDb(spamAssets: List<SpamAssetResponse>): List<SpamAssetDb> {
            val list = mutableListOf<SpamAssetDb>()
            spamAssets.forEach {
                list.add(SpamAssetDb(it))
            }
            return list
        }

        fun convertFromDb(spamAssets: List<SpamAssetDb>): List<SpamAssetResponse> {
            val list = mutableListOf<SpamAssetResponse>()
            spamAssets.forEach {
                list.add(it.convertFromDb())
            }
            return list
        }
    }
}