package com.wavesplatform.wallet.v2.data.model.db

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@RealmClass
open class Alias(
        @PrimaryKey
        @SerializedName("alias") var alias: String? = "",
        @SerializedName("address") var address: String? = "",
        var own: Boolean = false
) : RealmModel, Parcelable