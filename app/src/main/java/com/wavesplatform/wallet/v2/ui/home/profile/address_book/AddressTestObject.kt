package com.wavesplatform.wallet.v2.ui.home.profile.address_book

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AddressTestObject(
        var address: String,
        var name: String
) : Parcelable