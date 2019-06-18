package com.wavesplatform.wallet.v2.data.model.db

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.response.api.AssetInfoResponse
import com.wavesplatform.sdk.utils.notNull
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize
import java.util.*

@RealmClass
@Parcelize
open class AssetInfoDb(
        @SerializedName("ticker") var ticker: String? = "",
        @PrimaryKey
        @SerializedName("id") var id: String = "",
        @SerializedName("name") var name: String = "",
        @SerializedName("precision") var precision: Int = 0,
        @SerializedName("description") var description: String = "",
        @SerializedName("height") var height: Int = 0,
        @SerializedName("timestamp") var timestamp: String = "",
        @SerializedName("sender") var sender: String = "",
        @SerializedName("quantity") var quantity: Long = 0,
        @SerializedName("reissuable") var reissuable: Boolean = false,
        var isSpam: Boolean = false
) : Parcelable, RealmModel {

    constructor(assetInfo: AssetInfoResponse?) : this() {
        assetInfo.notNull {
            this.ticker = it.ticker
            this.id = it.id
            this.name = it.name
            this.precision = it.precision
            this.description = it.description
            this.height = it.height
            this.timestamp = it.timestamp.time.toString()
            this.sender = it.sender
            this.quantity = it.quantity
            this.reissuable = it.reissuable
            this.isSpam = it.isSpam
        }
    }

    fun convertFromDb(): AssetInfoResponse {
        return AssetInfoResponse(
                ticker = ticker,
                id = id,
                name = name,
                precision = precision,
                description = description,
                height = height,
                timestamp = Date(timestamp.toLong()),
                sender = sender,
                quantity = quantity,
                reissuable = reissuable,
                isSpam = isSpam
        )
    }

    companion object {
        
        fun convertToDb(assetInfo: List<AssetInfoResponse>): MutableList<AssetInfoDb> {
            val list = mutableListOf<AssetInfoDb>()
            assetInfo.forEach {
                list.add(AssetInfoDb(it))
            }
            return list
        }

        fun convertFromDb(assetInfo: List<AssetInfoDb>): MutableList<AssetInfoResponse> {
            val list = mutableListOf<AssetInfoResponse>()
            assetInfo.forEach {
                list.add(it.convertFromDb())
            }
            return list
        }
    }
}