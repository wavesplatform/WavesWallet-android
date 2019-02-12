package com.wavesplatform.wallet.v2.data.model.db

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@RealmClass
open class AddressBookUserDb(
        @PrimaryKey
        @SerializedName("address") var address: String = "",
        @SerializedName("name") var name: String = ""
) : RealmModel, Parcelable