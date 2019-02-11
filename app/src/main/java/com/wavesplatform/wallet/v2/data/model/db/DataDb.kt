package com.wavesplatform.wallet.v2.data.model.db

import com.google.gson.annotations.SerializedName
import io.realm.RealmModel
import io.realm.annotations.RealmClass

@RealmClass(name = "Data")
open class DataDb(
        @SerializedName("key") var key: String = "",
        @SerializedName("type") var type: String = "",
        @SerializedName("value") var value: String = ""
) : RealmModel