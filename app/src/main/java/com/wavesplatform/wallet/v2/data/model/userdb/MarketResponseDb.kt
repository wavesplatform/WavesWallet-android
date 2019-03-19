package com.wavesplatform.wallet.v2.data.model.userdb

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.net.model.response.AmountAssetInfo
import com.wavesplatform.sdk.net.model.response.MarketResponse
import com.wavesplatform.sdk.net.model.response.PriceAssetInfo
import com.wavesplatform.sdk.utils.notNull
import io.realm.RealmModel
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@RealmClass
open class MarketResponseDb(
        @PrimaryKey
        @SerializedName("id") var id: String? = "",
        @SerializedName("amountAsset") var amountAsset: String = "",
        @SerializedName("amountAssetName") var amountAssetName: String = "",
        @SerializedName("amountAssetShortName") var amountAssetShortName: String? = "",
        @SerializedName("amountAssetLongName") var amountAssetLongName: String? = "",
        @SerializedName("amountAssetDecimals") var amountAssetDecimals: Int = 0,
        @Ignore @SerializedName("amountAssetInfo")
        var amountAssetInfo: AmountAssetInfo = AmountAssetInfo(),
        @SerializedName("priceAsset") var priceAsset: String = "",
        @SerializedName("priceAssetName") var priceAssetName: String = "",
        @SerializedName("priceAssetShortName") var priceAssetShortName: String? = "",
        @SerializedName("priceAssetLongName") var priceAssetLongName: String? = "",
        @Ignore @SerializedName("priceAssetInfo")
        var priceAssetInfo: PriceAssetInfo = PriceAssetInfo(),
        @SerializedName("priceAssetDecimals") var priceAssetDecimals: Int = 0,
        @SerializedName("created") var created: Long = 0,
        @SerializedName("checked") var checked: Boolean = false,
        @SerializedName("popular") var popular: Boolean = false,
        @SerializedName("position") var position: Int = -1,
        @SerializedName("currentTimeFrame") var currentTimeFrame: Int? = null
) : Parcelable, RealmModel {

    constructor(market: MarketResponse?) : this() {
        market.notNull {
            this.id = it.id
            this.amountAsset = it.amountAsset
            this.amountAssetName = it.amountAssetName
            this.amountAssetShortName = it.amountAssetShortName
            this.amountAssetLongName = it.amountAssetLongName
            this.amountAssetDecimals = it.amountAssetDecimals
            this.amountAssetInfo = it.amountAssetInfo
            this.priceAsset = it.priceAsset
            this.priceAssetName = it.priceAssetName
            this.priceAssetShortName = it.priceAssetShortName
            this.priceAssetLongName = it.priceAssetLongName
            this.priceAssetInfo = it.priceAssetInfo
            this.priceAssetDecimals = it.priceAssetDecimals
            this.created = it.created
            this.checked = it.checked
            this.popular = it.popular
            this.position = it.position
            this.currentTimeFrame = it.currentTimeFrame
        }
    }

    fun convertFromDb(): MarketResponse {
        return MarketResponse(
                id = this.id,
                amountAsset = this.amountAsset,
                amountAssetName = this.amountAssetName,
                amountAssetShortName = this.amountAssetShortName,
                amountAssetLongName = this.amountAssetLongName,
                amountAssetDecimals = this.amountAssetDecimals,
                amountAssetInfo = this.amountAssetInfo,
                priceAsset = this.priceAsset,
                priceAssetName = this.priceAssetName,
                priceAssetShortName = this.priceAssetShortName,
                priceAssetLongName = this.priceAssetLongName,
                priceAssetInfo = this.priceAssetInfo,
                priceAssetDecimals = this.priceAssetDecimals,
                created = this.created,
                checked = this.checked,
                popular = this.popular,
                position = this.position,
                currentTimeFrame = this.currentTimeFrame
        )
    }

    companion object {

        fun convertToDb(markets: List<MarketResponse>): List<MarketResponseDb> {
            val list = mutableListOf<MarketResponseDb>()
            markets.forEach {
                list.add(MarketResponseDb(it))
            }
            return list
        }

        fun convertFromDb(markets: List<MarketResponseDb>): List<MarketResponse> {
            val list = mutableListOf<MarketResponse>()
            markets.forEach {
                list.add(it.convertFromDb())
            }
            return list
        }
    }
}