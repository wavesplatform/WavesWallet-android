package com.wavesplatform.wallet.v2.data.model.remote.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize

data class AliasesResponse(
    @SerializedName("__type") var type: String = "list",
    @SerializedName("data") var data: List<AliasData> = listOf()
)

data class AliasData(
    @SerializedName("__type") var type: String = "alias",
    @SerializedName("data") var alias: Alias = Alias()
)

@Parcelize
@RealmClass
open class Alias(
    @PrimaryKey
    @SerializedName("alias") var alias: String? = "",
    @SerializedName("address") var address: String? = "",
    var own: Boolean = false
) : RealmModel, Parcelable