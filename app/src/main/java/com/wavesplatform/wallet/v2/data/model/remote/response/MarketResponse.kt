package com.wavesplatform.wallet.v2.data.model.remote.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmModel
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@RealmClass
open class MarketResponse(
    @PrimaryKey
    @SerializedName("id")
    var id: String? = "",

    @SerializedName("amountAsset") var amountAsset: String = "",
    @SerializedName("amountAssetName") var amountAssetName: String = "",
    @SerializedName("amountAssetShortName") var amountAssetShortName: String? = "",
    @SerializedName("amountAssetLongName") var amountAssetLongName: String? = "",
    @SerializedName("amountAssetDecimals") var amountAssetDecimals: Int = 0,
    @Ignore @SerializedName("amountAssetInfo") var amountAssetInfo: AmountAssetInfo = AmountAssetInfo(),
    @SerializedName("priceAsset") var priceAsset: String = "",
    @SerializedName("priceAssetName") var priceAssetName: String = "",
    @SerializedName("priceAssetShortName") var priceAssetShortName: String? = "",
    @SerializedName("priceAssetLongName") var priceAssetLongName: String? = "",
    @Ignore @SerializedName("priceAssetInfo") var priceAssetInfo: PriceAssetInfo = PriceAssetInfo(),
    @SerializedName("priceAssetDecimals") var priceAssetDecimals: Int = 0,
    @SerializedName("created") var created: Long = 0,
    @SerializedName("checked") var checked: Boolean = false,
    @SerializedName("popular") var popular: Boolean = false,
    @SerializedName("position") var position: Int = -1,
    @SerializedName("currentTimeFrame") var currentTimeFrame: Int? = null
) : Parcelable, RealmModel

@Parcelize
data class AmountAssetInfo(
    @SerializedName("decimals") var decimals: Int = 0
) : Parcelable

@Parcelize
data class PriceAssetInfo(
    @SerializedName("decimals") var decimals: Int = 0
) : Parcelable