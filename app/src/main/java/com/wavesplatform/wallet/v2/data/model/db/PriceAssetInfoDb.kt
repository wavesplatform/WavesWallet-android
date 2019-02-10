package com.wavesplatform.wallet.v2.data.model.db

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmModel
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@RealmClass(name = "PriceAssetInfo")
data class PriceAssetInfoDb(@SerializedName("decimals") var decimals: Int = 0
) : RealmModel, Parcelable