package com.wavesplatform.wallet.v2.ui.home.profile.address_book

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
open class AddressBookUser(
        @PrimaryKey
        @SerializedName("address") var address: String = "",
        @SerializedName("name") var name: String = ""
) : Parcelable