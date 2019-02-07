package com.wavesplatform.sdk.model.response

import android.os.Parcelable

import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class TickerMarket : Parcelable {

    @SerializedName("symbol")
    var symbol: String? = null
    @SerializedName("amountAssetID")
    var amountAssetID: String? = null
    @SerializedName("amountAssetName")
    var amountAssetName: String? = null
    @SerializedName("amountAssetDecimals")
    var amountAssetDecimals: Int = 0
    @SerializedName("priceAssetID")
    var priceAssetID: String? = null
    @SerializedName("priceAssetName")
    var priceAssetName: String? = null
    @SerializedName("priceAssetDecimals")
    var priceAssetDecimals = 0
    @SerializedName("24h_open")
    var open24h: String? = "0.0"
    @SerializedName("24h_high")
    var high24h: String? = "0.0"
    @SerializedName("24h_low")
    var low24h: String? = "0.0"
    @SerializedName("24h_close")
    var close24h: String? = "0.0"
    @SerializedName("24h_vwap")
    var vwap24h: String? = null
    @SerializedName("24h_volume")
    var volume24h: String? = null
    @SerializedName("24h_priceVolume")
    var priceVolume24h: String? = null
    @SerializedName("timestamp")
    var timestamp: String? = null
}
