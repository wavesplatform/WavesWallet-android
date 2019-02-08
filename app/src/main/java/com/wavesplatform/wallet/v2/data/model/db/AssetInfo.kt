package com.wavesplatform.wallet.v2.data.model.db

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize

@RealmClass
@Parcelize
open class AssetInfo(
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
) : Parcelable, RealmModel