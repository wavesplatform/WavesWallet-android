package com.wavesplatform.wallet.v2.data.model.db

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.response.Alias
import com.wavesplatform.sdk.model.response.MarketResponse
import io.realm.RealmModel
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@RealmClass(name = "MarketResponse")
open class MarketResponseDb(
        @PrimaryKey
        @SerializedName("id")
        var id: String? = "",
        @SerializedName("amountAsset") var amountAsset: String = "",
        @SerializedName("amountAssetName") var amountAssetName: String = "",
        @SerializedName("amountAssetShortName") var amountAssetShortName: String? = "",
        @SerializedName("amountAssetLongName") var amountAssetLongName: String? = "",
        @SerializedName("amountAssetDecimals") var amountAssetDecimals: Int = 0,
        @Ignore @SerializedName("amountAssetInfo") var amountAssetInfo: AmountAssetInfoDb = AmountAssetInfoDb(),
        @SerializedName("priceAsset") var priceAsset: String = "",
        @SerializedName("priceAssetName") var priceAssetName: String = "",
        @SerializedName("priceAssetShortName") var priceAssetShortName: String? = "",
        @SerializedName("priceAssetLongName") var priceAssetLongName: String? = "",
        @Ignore @SerializedName("priceAssetInfo") var priceAssetInfo: PriceAssetInfoDb = PriceAssetInfoDb(),
        @SerializedName("priceAssetDecimals") var priceAssetDecimals: Int = 0,
        @SerializedName("created") var created: Long = 0,
        @SerializedName("checked") var checked: Boolean = false,
        @SerializedName("popular") var popular: Boolean = false,
        @SerializedName("position") var position: Int = -1,
        @SerializedName("currentTimeFrame") var currentTimeFrame: Int? = null
) : Parcelable, RealmModel {

        fun convertFromDb(): MarketResponse {
                return MarketResponse()
        }

        companion object {

                fun convertToDb(market: MarketResponse): MarketResponseDb {
                        return MarketResponseDb()
                }

                fun convertToDb(markets: List<MarketResponse>): List<MarketResponseDb> {
                        return listOf()
                }

                fun convertFromDb(markets: List<MarketResponseDb>): List<MarketResponse> {
                        return listOf()
                }
        }
}